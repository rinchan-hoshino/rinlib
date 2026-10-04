# RinLib

RinLib is the shared runtime library for RinChan Minecraft mods. Stable state and payment logic lives in one source set; small adapters handle differences in Minecraft and loader APIs.

## APIs

- `SharedOwnerState` reads and writes one shared entry in a UUID-keyed map.
- `StickyBoolean` keeps a boolean true once it becomes true.
- `ReentrantFlag` and `KeepInventoryProjection` manage nested, thread-local scopes.
- `DenominatedPayment` creates immutable payment and change plans from ordered inventory stacks.
- `StructureResources` and `StructureLifecycle` adapt template resources and their preparation lifecycle.
- `DamageState` checks and bounds item durability.
- `MobEffectState` applies effects to server players on supported NeoForge profiles.

API availability follows each [version profile](version-support.md).

## Build and test

Use Java 21 for the default Minecraft 1.21.1 build:

```sh
./gradlew build
./gradlew -PportableOnly :portable:test
python3 -m unittest discover -s tests -p 'test_*.py'
```

Build the supported release profiles from this checkout:

```sh
python3 tools/build_portable_profiles.py
python3 tools/build_portable_profiles.py --profile 1.21.1 --loader neoforge
python3 tools/build_portable_profiles.py --resume
python3 tests/verify_artifacts.py
```

Set `JAVA_8_HOME`, `JAVA_16_HOME`, `JAVA_17_HOME`, `JAVA_21_HOME`, and `JAVA_25_HOME` to the installed JDKs needed by the selected profiles. Build results, hashes and logs are written to `build/matrix`. Resume reuses artifacts only when the source fingerprint and artifact hashes match.

For local dedicated-server checks, see [runtime testing](../runtime-tests/README.md).

[Downloads](https://modrinth.com/mod/rinlib) · [Issues](https://github.com/rinchan-hoshino/rinlib/issues)
