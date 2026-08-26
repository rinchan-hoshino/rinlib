#!/usr/bin/env python3
"""Build deterministic loader-recognized RinLib JARs for an external profile matrix."""
from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import tempfile
import zipfile
from pathlib import Path

EPOCH = (1980, 1, 1, 0, 0, 0)
ROOT = Path(__file__).resolve().parents[1]


def minecraft_range(versions: list[str]) -> str:
    parsed = [tuple(int(part) for part in version.split('.')) for version in versions]
    low = min(parsed)
    high = list(max(parsed))
    high[-1] += 1
    return f"[{'.'.join(map(str, low))},{'.'.join(map(str, high))})"


def fabric_metadata(profile: dict, version: str) -> bytes:
    data = {
        "schemaVersion": 1,
        "id": "rinlib",
        "version": version,
        "name": "RinLib",
        "description": "Small shared contracts for RinChan Minecraft mods.",
        "authors": ["RinChan"],
        "contact": {
            "homepage": "https://modrinth.com/mod/rinlib",
            "sources": "https://github.com/rinchan-hoshino/rinlib",
            "issues": "https://github.com/rinchan-hoshino/rinlib/issues"
        },
        "license": "GPL-3.0-or-later",
        "environment": "*",
        "depends": {
            "fabricloader": ">=0.15.0",
            "minecraft": profile["game_versions"],
            "java": f">={profile['java']}"
        }
    }
    return (json.dumps(data, indent=2) + "\n").encode()


def toml_metadata(profile: dict, version: str) -> tuple[str, bytes]:
    loader = profile["loader"]
    dependency_field = "mandatory=true" if loader == "forge" else 'type="required"'
    path = "META-INF/mods.toml" if loader == "forge" or profile["minecraft"] == "1.20.4" else "META-INF/neoforge.mods.toml"
    text = f'''modLoader="lowcodefml"
loaderVersion="[1,)"
license="GPL-3.0-or-later"
issueTrackerURL="https://github.com/rinchan-hoshino/rinlib/issues"

[[mods]]
modId="rinlib"
version="{version}"
displayName="RinLib"
displayURL="https://modrinth.com/mod/rinlib"
authors="RinChan"
description="Small shared contracts for RinChan Minecraft mods."

[[dependencies.rinlib]]
modId="minecraft"
{dependency_field}
versionRange="{minecraft_range(profile['game_versions'])}"
ordering="NONE"
side="BOTH"
'''
    return path, text.encode()


def write_entry(out: zipfile.ZipFile, name: str, data: bytes) -> None:
    info = zipfile.ZipInfo(name, EPOCH)
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = 0o644 << 16
    out.writestr(info, data)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("profiles", type=Path)
    parser.add_argument("--output", type=Path, default=ROOT / "build" / "portable-profiles")
    args = parser.parse_args()
    matrix = json.loads(args.profiles.read_text())
    version = matrix["rinlib_version"]

    env = dict(__import__('os').environ)
    env.setdefault("JAVA_HOME", "/home/rin/.local/jdk-21")
    env["PATH"] = f"{env['JAVA_HOME']}/bin:{env.get('PATH', '')}"
    subprocess.run([
        str(ROOT / "gradlew"), "--no-daemon", "--no-configuration-cache",
        "-PportableOnly", ":portable:jar"
    ], cwd=ROOT, env=env, check=True)
    portable = ROOT / "portable" / "build" / "libs" / f"rinlib-portable-{version}.jar"
    if not portable.exists():
        candidates = list((ROOT / "portable" / "build" / "libs").glob("rinlib-portable-*.jar"))
        if len(candidates) != 1:
            raise SystemExit(f"portable JAR not found: {portable}")
        portable = candidates[0]

    args.output.mkdir(parents=True, exist_ok=True)
    for old in args.output.glob("*.jar"):
        old.unlink()
    with zipfile.ZipFile(portable) as source:
        base_entries = {
            name: source.read(name)
            for name in source.namelist()
            if not name.endswith('/') and name != "META-INF/MANIFEST.MF"
        }

    for profile in matrix["profiles"]:
        loader = profile["loader"]
        filename = f"rinlib-{version}+{profile['minecraft']}-{loader}.jar"
        target = args.output / filename
        entries = dict(base_entries)
        manifest = (
            "Manifest-Version: 1.0\r\n"
            "Implementation-Title: RinLib\r\n"
            f"Implementation-Version: {version}\r\n"
            "Implementation-Vendor: RinChan\r\n\r\n"
        ).encode()
        entries["META-INF/MANIFEST.MF"] = manifest
        entries["LICENSE"] = (ROOT / "LICENSE").read_bytes()
        if loader == "fabric":
            entries["fabric.mod.json"] = fabric_metadata(profile, version)
        else:
            path, data = toml_metadata(profile, version)
            entries[path] = data
        with zipfile.ZipFile(target, "w") as out:
            for name in sorted(entries):
                write_entry(out, name, entries[name])
        print(target)


if __name__ == "__main__":
    main()
