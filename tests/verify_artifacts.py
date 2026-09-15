#!/usr/bin/env python3
"""Verify production archives against the public API of published RinLib releases."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def public_api(jar, javap):
    with zipfile.ZipFile(jar) as archive:
        classes = sorted(n[:-6].replace('/', '.') for n in archive.namelist()
                         if n.startswith('dev/rinchan/rinlib/') and n.endswith('.class'))
    output = subprocess.check_output([javap, '-public', '-s', '-classpath', str(jar), *classes], text=True)
    # No bytecode bodies or constant values: only binary interface declarations.
    return [line.strip() for line in output.splitlines()
            if line.strip() and not line.startswith('Compiled from')]


def verify(record, baseline, javap):
    jar = Path(record['file'])
    assert hashlib.sha256(jar.read_bytes()).hexdigest() == record['sha256'], 'artifact hash mismatch'
    actual = public_api(jar, javap)
    assert actual == baseline['api'], 'public API changed'
    with zipfile.ZipFile(jar) as archive:
        names = archive.namelist()
        assert len(names) == len(set(names)), 'duplicate archive entries'
        assert not any('probe' in n.lower() or 'Harness' in n for n in names), 'test code in production'
        for name in names:
            if name.endswith('.class'):
                data = archive.read(name)
                assert int.from_bytes(data[6:8], 'big') <= baseline['java'] + 44, name + ': bytecode too new'
        assert archive.read('LICENSE') == (ROOT / 'LICENSE').read_bytes(), 'license mismatch'
        if 'fabric.mod.json' in names:
            metadata = json.loads(archive.read('fabric.mod.json'))
            assert metadata['id'] == 'rinlib' and metadata['contact']['issues'].endswith('/rinlib/issues')
            if record.get('profile') == '22w24a-fabric':
                assert metadata['depends']['minecraft'] == ['1.19.1-alpha.22.24.a'], 'snapshot version is not normalized for Fabric'
            for entry in metadata.get('entrypoints', {}).get('main', []):
                assert entry.replace('.', '/') + '.class' in names, 'missing entrypoint'
        else:
            descriptors = [n for n in names if n.endswith('mods.toml')]
            if descriptors:
                text = archive.read(descriptors[0]).decode()
                assert 'issueTrackerURL="https://github.com/rinchan-hoshino/rinlib/issues"' in text
                if '[[mixins]]' in text:
                    mixins = json.loads(archive.read('rinlib.mixins.json'))
                    for name in mixins['mixins']:
                        assert (mixins['package'] + '.' + name).replace('.', '/') + '.class' in names
    return jar


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--results', type=Path, default=ROOT / 'build/matrix/results.json')
    parser.add_argument('--javap', default='javap')
    args = parser.parse_args()
    contracts = json.loads((ROOT / 'tests/fixtures/public-api.json').read_text())
    results = json.loads(args.results.read_text())['results']
    for key, baseline in contracts["profiles"].items():
        baseline = dict(baseline, api=contracts["apis"][baseline["api"]])
        assert key in results and 'file' in results[key], 'missing successful build: ' + key
        try:
            verify(results[key], baseline, args.javap)
        except AssertionError as error:
            raise AssertionError(key + ': ' + str(error)) from error
    print(f"{len(contracts['profiles'])} production archives passed API, bytecode, metadata and test-isolation checks")


if __name__ == '__main__':
    main()
