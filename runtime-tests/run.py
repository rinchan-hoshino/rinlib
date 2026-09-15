#!/usr/bin/env python3
"""Run API assertions inside an isolated local Minecraft dedicated server."""
import argparse
import json
import os
from pathlib import Path
import shutil
import signal
import runpy
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def run(target, output, eula, legacy_server=None):
    mc, loader = target['minecraft'], target['loader']
    profile = next(p for p in json.loads((ROOT / 'support/profiles.json').read_text())['profiles']
                   if p['minecraft'] == target.get('profile', mc) and p['loader'] == loader)
    instance = output / f'{mc}-{loader}'
    instance.mkdir(parents=True, exist_ok=True)
    if eula.resolve() != (instance / 'eula.txt').resolve():
        shutil.copyfile(eula, instance / 'eula.txt')
    (instance / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=' + os.environ.get('RINLIB_PROBE_PORT', '25897') + '\nonline-mode=false\nlevel-type=minecraft:flat\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\nmax-tick-time=0\n')
    marker = instance / 'rinlib-runtime.pass'
    marker.unlink(missing_ok=True)
    family = ('26' if mc.startswith('26.') else '1.21.1') + '-' + loader if profile['kind'] == 'full' else profile['kind']
    if mc == '1.16.5':
        family = '1.16'
    wrapper = ROOT / 'gradlew'
    if loader == 'forge':
        wrapper = ROOT / 'profiles/wrapper-forge/gradlew'
    command = [str(wrapper), '-p', str(ROOT / 'profiles'),
               'runProbeServer' if loader == 'neoforge' else 'runServer', '-Pprofile=' + target.get('profile', mc), '-Ploader=' + loader,
               '-PruntimeTest', '-I', str(ROOT / f'runtime-tests/{loader}.init.gradle'),
               '-Dprobe.sources=' + str(ROOT / 'runtime-tests'), '-Dprobe.checks=' + str(ROOT / 'runtime-tests' / ('checks-' + family)),
               '-Dprobe.players=' + ('26-neoforge' if mc.startswith('26.') else 'modern'),
               '-Dprobe.instance=' + str(instance), '--project-cache-dir', str(ROOT / 'build/runtime-cache' / f'{mc}-{loader}'),
               '--no-daemon', '--no-configuration-cache', '--console=plain']
    if target.get('profile'):
        command.append('-PruntimeMinecraft=' + mc)
    if target.get('fabric_api'):
        command.append('-Dprobe.fabricApi=' + target['fabric_api'])
    env = os.environ.copy()
    java_key = 'JAVA_8_HOME' if mc == '1.12.2' else 'JAVA_25_HOME' if mc.startswith('26.') else 'JAVA_21_HOME'
    env['JAVA_HOME'] = env.get(java_key) or env.get('JAVA_HOME', '')
    if not env['JAVA_HOME']:
        raise ValueError('Set ' + java_key + ' or JAVA_HOME')
    paths = [v for k,v in env.items() if k.startswith('JAVA_') and k.endswith('_HOME')]
    command.append('-Porg.gradle.java.installations.paths=' + ','.join(paths))
    working_directory = ROOT
    if mc == '1.12.2':
        build_command = [str(ROOT / 'profiles/wrapper-legacy/gradlew'), '-p', str(ROOT / 'profiles'),
                         'build', 'runtimeProbeJar', 'reobfRuntimeProbeJar', '-Pprofile=1.12.2', '-Ploader=forge',
                         '-I', str(ROOT / 'runtime-tests/legacy.init.gradle'), '-Dprobe.sources=' + str(ROOT / 'runtime-tests'),
                         '--no-daemon', '--console=plain']
        with (instance / 'probe-build.log').open('w') as log:
            subprocess.run(build_command, cwd=ROOT, env=env, stdout=log, stderr=subprocess.STDOUT, check=True, timeout=600)
        forge_jars = list(legacy_server.glob('forge-1.12.2-*-universal.jar'))
        if len(forge_jars) != 1 or not (legacy_server / 'libraries').is_dir():
            raise ValueError('Expected one installed Forge 1.12.2 server and its libraries')
        for source in [forge_jars[0], legacy_server / 'minecraft_server.1.12.2.jar']:
            shutil.copyfile(source, instance / source.name)
        libraries = instance / 'libraries'
        if not libraries.exists():
            libraries.symlink_to(legacy_server / 'libraries', target_is_directory=True)
        mods = instance / 'mods'
        mods.mkdir(exist_ok=True)
        version = runpy.run_path(str(ROOT / 'tools/build_portable_profiles.py'))['release_version']()
        for name in ['rinlib-forge-' + version + '+1.12.2.jar', 'rinlib-runtime-probe-1.0.0.jar']:
            shutil.copyfile(ROOT / 'build/profiles/1.12.2-forge/libs' / name, mods / name)
        command = [str(Path(env['JAVA_HOME']) / 'bin/java'), '-Xmx1G', '-jar', forge_jars[0].name, 'nogui']
        working_directory = instance
    log_path = instance / 'run.log'
    with log_path.open('w') as log:
        process = subprocess.Popen(command, cwd=working_directory, env=env, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
        try:
            code = process.wait(timeout=600)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGTERM)
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait()
            code = -1
        finally:
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL)
                    process.wait()
    text = log_path.read_text()
    passed = code == 0 and marker.exists() and 'RINLIB_RUNTIME_PASS' in text and 'RINLIB_RUNTIME_FAIL' not in text
    result = dict(target, passed=passed, exit_code=code, assertions=text.count('RINLIB_ASSERT_PASS'), log=str(log_path))
    (instance / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
    print(f'{mc}-{loader}: {"PASS" if passed else "FAIL"}', flush=True)
    return passed


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile')
    parser.add_argument('--loader')
    parser.add_argument('--legacy-server', type=Path, help='Installed Forge 1.12.2 server directory for legacy tests')
    parser.add_argument('--eula', type=Path, required=True, help='Existing accepted eula.txt for these local tests')
    parser.add_argument('--output', type=Path, default=ROOT / 'build/runtime')
    args = parser.parse_args()
    if 'eula=true' not in args.eula.read_text():
        parser.error('An accepted eula.txt is required')
    targets = [p for p in json.loads((ROOT / 'runtime-tests/profiles.json').read_text())
               if (not args.profile or p['minecraft'] == args.profile) and (not args.loader or p['loader'] == args.loader)]
    if not targets:
        parser.error('No runtime profile matches')
    if any(p['minecraft'] == '1.12.2' for p in targets) and not args.legacy_server:
        parser.error('--legacy-server is required for Minecraft 1.12.2')
    passed = [run(p, args.output.resolve(), args.eula.resolve(),
                  args.legacy_server.resolve() if args.legacy_server else None) for p in targets]
    if not all(passed):
        raise SystemExit(1)


if __name__ == '__main__':
    main()
