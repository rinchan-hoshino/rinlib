# RinLib

RinLib is the shared runtime library for RinChan Minecraft mods.

The library keeps stable cross-version contracts in one place so gameplay mods can remain small and focused. Its APIs include durability state, fixed shared-owner map operations, sticky boolean projection, reentrant scoped state, death-scoped `keepInventory` projection, and version-adapted mob-effect lookup.

## Version branches

Minecraft-specific source is retained on `mc/<minecraft-version>` branches. Public artifacts and their exact loader/game-version metadata are listed on [Modrinth](https://modrinth.com/mod/rinlib).

## Build

```bash
./gradlew build
```

For local development of dependent mods:

```bash
./gradlew publishToMavenLocal
```

## Local CI

Run the repository checks with:

```bash
./scripts/ci.sh
```

The repository includes `.githooks/pre-commit`; enable it once per clone with:

```bash
git config core.hooksPath .githooks
```
