# Development

RinLib is a library embedded by RinChan mods. It is not installed by players.

## Layout

- `common/` holds pure Java classes and unit tests (`common/src/test`).
- `adapters/<minecraft>/dev/rinchan/rinlib/api` holds the stable `dev.rinchan.rinlib.api`
  implementation for each supported Minecraft version. Public signatures must match across
  versions; `python3 tools/check_adapter_api.py` checks this.
- `adapters/shared`, `adapters/structure-*`, `adapters/commands-modern`, and similar directories
  hold Minecraft-version-specific sources that are selected per target.
- `fabric/` and `neoforge/` hold loader entrypoints and build scripts for the default 1.21.1 build.
- `support/profiles.json` is the single table of exact Minecraft/loader targets. It names the Java
  version, pinned dependencies, and the complete source-directory list for each target.
- `profiles/` contains the Gradle scripts that build non-default targets.
- `tests/` contains Python checks for API compatibility, profile metadata, and server smoke tests.
- `structure-tests/` and `api-tests/` are JUnit sources added to the `test` source set by the
  build scripts.

## Build

Use a full JDK 21 for the default 1.21.1 build:

```sh
./gradlew :common:test :fabric:build :neoforge:build
python3 -m unittest discover -s tests -p 'test_*.py'
python3 tools/check_adapter_api.py
```

Build one additional target directly:

```sh
./gradlew -p profiles -Pprofile=1.20.1 -Ploader=fabric build
profiles/wrapper-forge/gradlew -p profiles -Pprofile=1.20.1 -Ploader=forge build
```

For 26.x, run Gradle with a full JDK 25. Forge uses the checked-in Gradle 8 wrapper.
1.12.2 uses `profiles/wrapper-legacy/gradlew` with JDK 8. Other targets use the root wrapper.
Pass `-Porg.gradle.java.installations.paths=...` when a required target JDK is not discovered.

Outputs go to `build/profiles/<minecraft>-<loader>`. The default root build writes to
`fabric/build` and `neoforge/build`.

## Adding API

- New stable adapters go in `adapters/<minecraft>/dev/rinchan/rinlib/api`.
- Select directories through the table in `support/profiles.json`. Do not add Minecraft
  version branches to Gradle scripts.
- ID arguments are `String`.
- Retained compatibility entrypoints delegate to the `api` package. Do not keep a second copy
  of the behavior in them.
- Function execution must run on the server thread. An existing command context is removed and
  restored in `finally`, including when the callback throws.

## Embedding in a mod

Mods embed RinLib and do not require a separate install.

- Fabric uses Loom `include`.
- NeoForge uses `jarJar`.
- The dependency version is pinned to a published RinLib release.
- Local builds may point at a jar with `-PrinlibJar=<path>` (Fabric) or `-PrinlibNeoJar=<path>` (NeoForge).

## Target coverage

- The 2.0 table lists 69 exact Minecraft/loader combinations.
- The complete `dev.rinchan.rinlib.api` contract covers 17 Minecraft versions from 1.20.6 to 26.2,
  across 44 loader targets.
- The other targets keep only the classes their consumers need. The older item NBT model and
  legacy Forge type names need separate design before they can use the full API.
- Old `dev.rinchan.rinlib.minecraft` entrypoints are deprecated forwarding adapters. They stay
  until consumers migrate.

## Machine-local notes

Builds that need a cached native artifact missing from Maven Central should use a local Maven
directory and an init script outside the repository. Do not commit such substitutions.
