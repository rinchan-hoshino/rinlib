# RinLib

RinLib is the shared runtime library for RinChan Minecraft mods.

The library keeps cross-loader contracts in one place so gameplay mods can stay small and focused. The first published API covers shared durability-state helpers used by Keep My Sword.

## Supported targets

This branch implements Minecraft 1.20.1 for Fabric, Forge, and NeoForge.

The full target matrix is tracked in [docs/version-support.md](docs/version-support.md): 1.7.10, 1.12.2, 1.16.5, 1.20.1, 1.21.1, and the latest 26.1.x line, with Forge capped at 1.20.1, NeoForge starting at 1.20.1, and Fabric starting at 1.16.5.

## Build

```bash
./gradlew build
```

For local development of dependent mods:

```bash
./gradlew publishToMavenLocal
```
