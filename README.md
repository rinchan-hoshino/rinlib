# RinLib

RinLib is the shared runtime library for RinChan Minecraft mods.

The library keeps cross-loader contracts in one place so gameplay mods can stay small and focused. The first published API covers shared durability-state helpers used by Keep My Sword.

## Supported target

- Minecraft 1.21.1
- Fabric
- NeoForge
- Java 21

New Minecraft versions should be added as separate release lines after the 1.21.1 line is stable.

## Build

```bash
./gradlew build
```

For local development of dependent mods:

```bash
./gradlew publishToMavenLocal
```
