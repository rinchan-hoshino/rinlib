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
    if directory.parent.parent == ROOT / 'adapters/api':
        sources = [path for path in sources if path.name == directory.parent.name + '.java']
        common = directory.parent / 'all'
        if not sources and directory.name != 'all':
            sources = sorted(common.rglob(directory.parent.name + '.java'))
    if not sources:
        raise ValueError(f'No API sources: {directory}')
    for path in sources:
        source = re.sub(r'/\*.*?\*/|//[^\n]*', '', path.read_text(), flags=re.S)
        package = re.search(r'\bpackage\s+([\w.]+)\s*;', source)
        if package is None or package.group(1) not in ('dev.rinchan.rinlib.api', 'dev.rinchan.rinlib.v2', 'dev.rinchan.rinlib.v2.net'):
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
    groups = [args.directories] if args.directories else []
    if not groups:
        profiles = json.loads((ROOT / 'support/profiles.json').read_text())['profiles']
        classes = {}
        for profile in profiles:
            for name, interval in profile.get('api', {}).items():
                classes.setdefault(name, set()).add(ROOT / 'adapters/api' / name / interval)
        groups = [sorted(directories) for directories in classes.values()]
    if not groups:
        parser.exit(1, 'No stable API adapters configured\n')
    declarations = 0
    try:
        for directories in groups:
            baseline = public_api(directories[0])
            declarations += len(baseline)
            for directory in directories[1:]:
                actual = public_api(directory)
                if actual != baseline:
                    parser.exit(1, '\n'.join(difflib.unified_diff(baseline, actual,
                                fromfile=str(directories[0]), tofile=str(directory))) + '\n')
    except ValueError as error:
        parser.exit(1, str(error) + '\n')
    print(f'{sum(map(len, groups))} adapter directories in {len(groups)} groups agree on '
          f'{declarations} public declarations')


if __name__ == '__main__':
    main()
