#!/usr/bin/env python3
"""Generate loader metadata from the selected native source adapters."""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def release_version():
    return next(line.split('=', 1)[1].split('+', 1)[0]
                for line in (ROOT / 'gradle.properties').read_text().splitlines()
                if line.startswith('mod_version='))


def metadata(target):
    loader = target['loader']
    adapters = target['adapters']
    configs = {}
    for directory in adapters:
        resources = ROOT / directory / 'resources'
        for path in resources.glob('*.mixins.json'):
            config = json.loads(path.read_text())
            config['compatibilityLevel'] = 'JAVA_' + str(target['java'])
            if loader == 'forge':
                config['refmap'] = 'rinlib.structure.refmap.json'
            configs[path.name] = config
    if target.get('api', {}).get('Rin'):
        config = json.loads((ROOT / loader / 'src/main/resources/rinlib.mixins.json').read_text())
        config['compatibilityLevel'] = 'JAVA_' + str(target['java'])
        configs['rinlib.mixins.json'] = config
    elif 'adapters/compat-1.21' in adapters or 'adapters/26' in adapters:
        config = json.loads((ROOT / 'neoforge/src/main/resources/rinlib.mixins.json').read_text())
        config = dict(config, package='dev.rinchan.rinlib.mixin', mixins=['GameRulesMixin'])
        config.pop('client', None)
        config['compatibilityLevel'] = 'JAVA_' + str(target['java'])
        configs['rinlib.mixins.json'] = config
    # Mixin reserves its package: keep the legacy GameRules hook outside the public v2 API namespace.
    if target.get('api', {}).get('Rin') and loader == 'neoforge':
        configs['rinlib.gamerules.mixins.json'] = dict(required=True, minVersion='0.8',
            **{'package': 'dev.rinchan.rinlib.mixin'}, compatibilityLevel='JAVA_' + str(target['java']),
            mixins=['GameRulesMixin'], injectors=dict(defaultRequire=1))
    entries = {name: json.dumps(value, indent=2) + '\n' for name, value in configs.items()}
    version = release_version() + '+' + target['minecraft']
    if loader == 'fabric':
        # Mojang's snapshot label and Fabric's normalized game version differ.
        game = '1.19.1-alpha.22.24.a' if target['minecraft'] == '22w24a' else target['minecraft']
        data = dict(schemaVersion=1, id='rinlib', version=version, name='RinLib',
                    description='Cross-version Minecraft adapters for RinChan mods.',
                    authors=['RinChan'], license='GPL-3.0-or-later', environment='*',
                    contact=dict(sources='https://github.com/rinchan-hoshino/rinlib',
                                 issues='https://github.com/rinchan-hoshino/rinlib/issues'),
                    entrypoints=dict(main=['dev.rinchan.rinlib.fabric.RinLibFabric']),
                    depends=dict(fabricloader='>=' + target['dependencies']['fabric_loader'],
                                 minecraft=[game], java='>=' + str(target['java'])))
        if target.get('api', {}).get('Rin'):
            data['entrypoints']['client'] = ['dev.rinchan.rinlib.v2.RinFabricClient']
            for module in ['fabric-api-base', 'fabric-entity-events-v1', 'fabric-events-interaction-v0', 'fabric-lifecycle-events-v1', 'fabric-networking-api-v1', 'fabric-resource-loader-v0'] + (['fabric-resource-loader-v1'] if target['minecraft'].startswith('26.') else []):
                data['depends'][module] = '*'
        if configs:
            data['mixins'] = list(configs)
        entries['fabric.mod.json'] = json.dumps(data, indent=2) + '\n'
    else:
        required = 'mandatory=true' if loader == 'forge' else 'type="required"'
        text = f'''modLoader="javafml"
loaderVersion="[1,)"
license="GPL-3.0-or-later"
issueTrackerURL="https://github.com/rinchan-hoshino/rinlib/issues"
[[mods]]
modId="rinlib"
version="{version}"
displayName="RinLib"
authors="RinChan"
description="Cross-version Minecraft adapters for RinChan mods."
'''
        if loader != 'forge':
            text += ''.join(f'\n[[mixins]]\nconfig="{name}"\n' for name in configs)
        for mod, constraint in [('minecraft', '[' + target['minecraft'] + ']'),
                                (loader, '[' + target['dependencies'][loader] + ',)')]:
            text += f'\n[[dependencies.rinlib]]\nmodId="{mod}"\n{required}\nversionRange="{constraint}"\nordering="NONE"\nside="BOTH"\n'
        # NeoForge 20.4 still reads the Forge descriptor filename.
        path = 'META-INF/mods.toml' if loader == 'forge' or target['minecraft'] == '1.20.4' else 'META-INF/neoforge.mods.toml'
        entries[path] = text
    return entries


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('minecraft')
    parser.add_argument('loader', choices=['fabric', 'forge', 'neoforge'])
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    profiles = json.loads((ROOT / 'support/profiles.json').read_text())['profiles']
    target = next((p for p in profiles if p['minecraft'] == args.minecraft and p['loader'] == args.loader), None)
    if target is None:
        parser.error('Unsupported Minecraft/loader target')
    for name, text in metadata(target).items():
        path = args.output / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)


if __name__ == '__main__':
    main()
