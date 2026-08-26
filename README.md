# RinLib

RinLib is the shared runtime library for RinChan Minecraft mods.

The library keeps stable cross-version contracts in one place so gameplay mods remain small and focused. Its portable APIs include scoped state helpers and the denomination-based payment planner used by Paid Waystones. Minecraft-specific profiles may additionally expose the state and effect bridges required by their dependent mods.

## Portable payment planning

`DenominatedPayment` converts ordered inventory-stack values into an immutable removal-and-change plan. It has no Minecraft or loader dependency and targets Java 17 bytecode.

## Build

Run the focused portable tests:

```bash
./gradlew -PportableOnly :portable:test
```

Assemble loader metadata profiles from a consumer-owned matrix:

```bash
python3 tools/build_portable_profiles.py /path/to/profiles.json
```

Public artifacts and their exact loader/game-version metadata are listed on [Modrinth](https://modrinth.com/mod/rinlib).
