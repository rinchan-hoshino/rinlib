#!/usr/bin/env python3
"""Build RinLib profiles entirely from this checkout, with isolated outputs."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import subprocess
import shutil
import tempfile
import zipfile
from pathlib import Path

EPOCH = (1980, 1, 1, 0, 0, 0)
ROOT = Path(__file__).resolve().parents[1]

def minecraft_range(versions: list[str]) -> str:
    if len(versions) == 1:
        return f'[{versions[0]}]'
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
            "fabricloader": ">=" + profile.get("fabric_loader_version", "0.15.0"),
            "minecraft": profile.get("fabric_game_versions", profile["game_versions"]),
            "java": f">={profile['java']}"
        }
    }
    if profile["kind"] != "portable":
        data["entrypoints"] = {"main": ["dev.rinchan.rinlib.fabric.RinLibFabric"]}
    return (json.dumps(data, indent=2) + "\n").encode()


def toml_metadata(profile: dict, version: str, has_mixins: bool) -> tuple[str, bytes]:
    loader = profile["loader"]
    dependency_field = "mandatory=true" if loader == "forge" else 'type="required"'
    path = "META-INF/mods.toml" if loader == "forge" or profile["minecraft"] == "1.20.4" else "META-INF/neoforge.mods.toml"
    mod_loader = "lowcodefml" if profile["kind"] == "portable" else "javafml"
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
    if profile["kind"] != "portable":
        field = "forge_version" if loader == "forge" else "neo_version"
        text += f'\n[[dependencies.rinlib]]\nmodId="{loader}"\n{dependency_field}\nversionRange="[{profile[field]},)"\nordering="NONE"\nside="BOTH"\n'
    return path, text.encode()


def write_entry(out: zipfile.ZipFile, name: str, data: bytes) -> None:
    info = zipfile.ZipInfo(name, EPOCH)
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = 0o644 << 16
    out.writestr(info, data)


def release_version():
    for line in (ROOT / "gradle.properties").read_text().splitlines():
        if line.startswith("mod_version="):
            return line.split("=", 1)[1].split("+", 1)[0]
    raise ValueError("mod_version is missing")


def profiles():
    return json.loads((ROOT / "support/profiles.json").read_text())["profiles"]


def metadata(profile, version):
    if profile["loader"] == "fabric":
        return {"fabric.mod.json": fabric_metadata(profile, version)}
    full = profile["kind"] == "full" and profile["loader"] == "neoforge"
    name, data = toml_metadata(profile, version, full)
    entries = {name: data}
    if full:
        entries["rinlib.mixins.json"] = (ROOT / "neoforge/src/main/resources/rinlib.mixins.json").read_bytes()
    return entries


def source_fingerprint():
    names = subprocess.check_output(
        ["git", "ls-files", "-co", "--exclude-standard", "-z",
         "common", "adapters", "profiles", "support", "tools", "gradle", "build.gradle", "settings.gradle",
         "gradle.properties", "LICENSE", "gradlew", "fabric/src", "neoforge/src", "fabric/build.gradle",
         "neoforge/build.gradle", "portable/build.gradle"], cwd=ROOT).decode().split("\0")
    digest = hashlib.sha256()
    for name in sorted(set(names)):
        path = ROOT / name
        if name and path.is_file():
            digest.update(name.encode() + b"\0" + path.read_bytes())
    return digest.hexdigest()


def gradle(command, java, log_path):
    env = os.environ.copy()
    if env.get(f"JAVA_{java}_HOME"):
        env["JAVA_HOME"] = env[f"JAVA_{java}_HOME"]
    paths = [value for key, value in env.items() if key.startswith("JAVA_") and key.endswith("_HOME")]
    if paths:
        command += ["-Porg.gradle.java.installations.paths=" + ",".join(paths)]
    with log_path.open("w") as log:
        options = ["--no-daemon", "--console=plain"]
        if java != 8:
            options.append("--no-configuration-cache")
        subprocess.run(command + options,
                       cwd=ROOT, env=env, stdout=log, stderr=subprocess.STDOUT, check=True)


def build(profile, output, portable_jar):
    key = f"{profile['minecraft']}-{profile['loader']}"
    version = release_version() + "+" + profile["minecraft"]
    cell = output / key
    cell.mkdir(parents=True, exist_ok=True)
    if profile["kind"] == "portable":
        with zipfile.ZipFile(portable_jar) as source:
            entries = {name: source.read(name) for name in source.namelist()
                       if not name.endswith('/') and name != 'META-INF/MANIFEST.MF'}
        entries.update(metadata(profile, version))
        entries["LICENSE"] = (ROOT / "LICENSE").read_bytes()
        artifact = cell / f"rinlib-{profile['loader']}-{version}.jar"
        # Replace only this profile's artifact after a complete successful write.
        with tempfile.NamedTemporaryFile(dir=cell, suffix=".jar", delete=False) as temporary:
            staged = Path(temporary.name)
        try:
            with zipfile.ZipFile(staged, "w") as jar:
                for name in sorted(entries):
                    write_entry(jar, name, entries[name])
            staged.replace(artifact)
        finally:
            staged.unlink(missing_ok=True)
    else:
        legacy = profile["kind"] == "legacy"
        wrapper = ROOT / "gradlew"
        if profile["loader"] == "forge":
            wrapper = ROOT / "profiles" / ("wrapper-legacy" if legacy else "wrapper-forge") / "gradlew"
        gradle([str(wrapper), "-p", str(ROOT / "profiles"), "build",
                "-Pprofile=" + profile["minecraft"], "-Ploader=" + profile["loader"],
                "--project-cache-dir", str(ROOT / "build/project-cache" / key)],
               8 if legacy else 25 if profile["java"] == 25 else 21, cell / "build.log")
        candidates = [p for p in (ROOT / "build/profiles" / key / "libs").glob("*.jar")
                      if p.name == f"rinlib-{profile['loader']}-{version}.jar"]
        if len(candidates) != 1:
            raise RuntimeError(f"Expected one production JAR for {key}: {candidates}")
        artifact = cell / candidates[0].name
        shutil.copy2(candidates[0], artifact)
    return {"profile": key, "file": str(artifact.resolve()),
            "sha256": hashlib.sha256(artifact.read_bytes()).hexdigest()}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--profile")
    parser.add_argument("--loader", choices=["fabric", "forge", "neoforge"])
    parser.add_argument("--metadata", nargs=2, metavar=("MINECRAFT", "LOADER"))
    parser.add_argument("--output", type=Path, default=ROOT / "build/matrix")
    parser.add_argument("--resume", action="store_true")
    args = parser.parse_args()
    selected = profiles()
    if args.metadata:
        mc, loader = args.metadata
        target = next(p for p in selected if p["minecraft"] == mc and p["loader"] == loader)
        for name, data in metadata(target, release_version() + "+" + mc).items():
            path = args.output / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        return
    selected = [p for p in selected if (not args.profile or p["minecraft"] == args.profile)
                and (not args.loader or p["loader"] == args.loader)]
    if not selected:
        parser.error("No supported profile matches")
    args.output = args.output.resolve()
    args.output.mkdir(parents=True, exist_ok=True)
    fingerprint = source_fingerprint()
    receipt = args.output / "results.json"
    previous = json.loads(receipt.read_text()) if args.resume and receipt.exists() else {}
    results = previous.get("results", {}) if previous.get("source_fingerprint") == fingerprint else {}
    portable = ROOT / "portable/build/libs/rinlib-portable.jar"
    if any(p["kind"] == "portable" for p in selected):
        gradle([str(ROOT / "gradlew"), "-PportableOnly", ":portable:test", ":portable:jar"],
               21, args.output / "portable-build.log")
    failed = []
    for profile in selected:
        key = f"{profile['minecraft']}-{profile['loader']}"
        old = results.get(key, {})
        if old.get("sha256") and Path(old["file"]).is_file() and \
                hashlib.sha256(Path(old["file"]).read_bytes()).hexdigest() == old["sha256"]:
            print(key + ": unchanged", flush=True)
            continue
        try:
            results[key] = build(profile, args.output, portable)
            print(key + ": built", flush=True)
        except Exception as error:
            results[key] = {"error": str(error)}
            failed.append(key)
            print(key + ": FAILED: " + str(error), flush=True)
        if source_fingerprint() != fingerprint:
            raise RuntimeError("Sources changed during the build; rebuild before using these results")
        receipt.write_text(json.dumps({"source_fingerprint": fingerprint, "results": results}, indent=2) + "\n")
    if failed:
        raise SystemExit("Failed: " + ", ".join(failed))


if __name__ == "__main__":
    main()
