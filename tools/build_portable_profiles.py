#!/usr/bin/env python3
"""Build deterministic loader-recognized RinLib JARs for an external profile matrix.

Formal anchor profiles merge the last verified Minecraft-bound release with the
current portable contracts. This preserves version-specific DamageState and
NeoForge state bridges without pretending that those classes are portable.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import subprocess
import urllib.request
import zipfile
from pathlib import Path

EPOCH = (1980, 1, 1, 0, 0, 0)
ROOT = Path(__file__).resolve().parents[1]
IGNORED_BASE_ENTRIES = {
    "META-INF/MANIFEST.MF",
    "META-INF/mods.toml",
    "META-INF/neoforge.mods.toml",
    "fabric.mod.json",
}


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


def toml_metadata(profile: dict, version: str, has_mixins: bool) -> tuple[str, bytes]:
    loader = profile["loader"]
    dependency_field = "mandatory=true" if loader == "forge" else 'type="required"'
    path = "META-INF/mods.toml" if loader == "forge" or profile["minecraft"] == "1.20.4" else "META-INF/neoforge.mods.toml"
    mod_loader = "javafml" if has_mixins and loader == "neoforge" else "lowcodefml"
    mixins = '\n[[mixins]]\nconfig="rinlib.mixins.json"\n' if has_mixins else ""
    text = f'''modLoader="{mod_loader}"
loaderVersion="[1,)"
license="GPL-3.0-or-later"
issueTrackerURL="https://github.com/rinchan-hoshino/rinlib/issues"
{mixins}
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


def read_entries(path: Path, ignored: set[str] | None = None) -> dict[str, bytes]:
    ignored = ignored or set()
    with zipfile.ZipFile(path) as source:
        return {
            name: source.read(name)
            for name in source.namelist()
            if not name.endswith('/') and name not in ignored and not name.startswith("META-INF/services/")
        }


def verified_download(spec: dict, cache: Path) -> Path:
    cache.mkdir(parents=True, exist_ok=True)
    filename = spec["url"].rsplit('/', 1)[-1].replace('%2B', '+')
    target = cache / filename
    if not target.exists() or hashlib.sha512(target.read_bytes()).hexdigest() != spec["sha512"]:
        with urllib.request.urlopen(spec["url"], timeout=60) as response:
            target.write_bytes(response.read())
    actual = hashlib.sha512(target.read_bytes()).hexdigest()
    if actual != spec["sha512"]:
        raise SystemExit(f"anchor SHA-512 mismatch for {target}: {actual}")
    return target


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("profiles", type=Path)
    parser.add_argument("--anchor-bases", type=Path)
    parser.add_argument("--output", type=Path, default=ROOT / "build" / "portable-profiles")
    args = parser.parse_args()
    matrix = json.loads(args.profiles.read_text())
    version = matrix["rinlib_version"]
    anchor_specs = {}
    if args.anchor_bases:
        anchor_specs = json.loads(args.anchor_bases.read_text())["bases"]

    env = dict(os.environ)
    env.setdefault("JAVA_HOME", "/home/rin/.local/jdk-21")
    env["PATH"] = f"{env['JAVA_HOME']}/bin:{env.get('PATH', '')}"
    subprocess.run([
        str(ROOT / "gradlew"), "--no-daemon", "--no-configuration-cache",
        "-PportableOnly", ":portable:test", ":portable:jar"
    ], cwd=ROOT, env=env, check=True)
    portable_candidates = list((ROOT / "portable" / "build" / "libs").glob("rinlib-portable-*.jar"))
    if len(portable_candidates) != 1:
        raise SystemExit(f"expected one portable JAR, found {portable_candidates}")
    portable_entries = read_entries(portable_candidates[0], {"META-INF/MANIFEST.MF"})

    args.output.mkdir(parents=True, exist_ok=True)
    for old in args.output.glob("*.jar"):
        old.unlink()
    cache = ROOT / "build" / "formal-anchor-cache"

    for profile in matrix["profiles"]:
        loader = profile["loader"]
        key = f"{profile['minecraft']}-{loader}"
        filename = f"rinlib-{version}+{key}.jar"
        target = args.output / filename
        entries: dict[str, bytes] = {}
        if key in anchor_specs:
            anchor = verified_download(anchor_specs[key], cache)
            entries.update(read_entries(anchor, IGNORED_BASE_ENTRIES))
        entries.update(portable_entries)
        has_mixins = "rinlib.mixins.json" in entries
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
            path, data = toml_metadata(profile, version, has_mixins)
            entries[path] = data
        with zipfile.ZipFile(target, "w") as out:
            for name in sorted(entries):
                write_entry(out, name, entries[name])
        print(target)


if __name__ == "__main__":
    main()
