#!/usr/bin/env python3
"""Compare source-level public contracts without downloading Minecraft dependencies."""
import argparse
import difflib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def public_api(directory):
    result = []
    sources = sorted(directory.rglob('*.java'))
    if not sources:
        raise ValueError(f'No API sources: {directory}')
    for path in sources:
        source = re.sub(r'/\*.*?\*/|//[^\n]*', '', path.read_text(), flags=re.S)
        package = re.search(r'\bpackage\s+([\w.]+)\s*;', source)
        if package is None or package.group(1) != 'dev.rinchan.rinlib.api':
            raise ValueError(f'{path}: expected package dev.rinchan.rinlib.api')
        imports = dict((name.rsplit('.', 1)[-1], name) for name in
                       re.findall(r'\bimport\s+([\w.]+)\s*;', source))
        declarations = re.findall(r'\bpublic\s+([^{};]+)(?=[{;])', source)
        for declaration in declarations:
            # Parameter names and implementation bodies are not API contracts.
            declaration = re.sub(r'([\w<>.?\[\]]+)\s+\w+(?=\s*[,)]\s*)', r'\1', declaration)
            for simple, qualified in imports.items():
                declaration = re.sub(r'\b' + simple + r'\b', qualified, declaration)
            declaration = re.sub(r'\s+', ' ', declaration).strip()
            if re.search(r'\b(ResourceLocation|Identifier)\b', declaration):
                raise ValueError(f'{path}: unstable identifier in public API: {declaration}')
            result.append(path.name + ': public ' + declaration)
    return sorted(result)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directories', nargs='*', type=Path)
    args = parser.parse_args()
    directories = args.directories
    if not directories:
        profiles = json.loads((ROOT / 'support/profiles.json').read_text())['profiles']
        directories = sorted({ROOT / directory / 'dev/rinchan/rinlib/api'
                              for profile in profiles for directory in profile.get('adapters', [])
                              if directory == 'adapters/' + profile['minecraft']})
    if not directories:
        parser.exit(1, 'No stable API adapters configured\n')
    try:
        baseline = public_api(directories[0])
        for directory in directories[1:]:
            actual = public_api(directory)
            if actual != baseline:
                parser.exit(1, '\n'.join(difflib.unified_diff(baseline, actual,
                            fromfile=str(directories[0]), tofile=str(directory))) + '\n')
    except ValueError as error:
        parser.exit(1, str(error) + '\n')
    print(f'{len(directories)} adapter directories agree on {len(baseline)} public declarations')


if __name__ == '__main__':
    main()
