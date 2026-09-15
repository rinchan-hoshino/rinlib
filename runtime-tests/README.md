# Dedicated-server API tests

These checks start isolated local Minecraft dedicated servers and run assertions after server startup. They exercise shared-map identity and removal, sticky state, and the APIs available in each profile. Full NeoForge checks also create a server-side player, apply an effect, and verify that the game-rule mixin projects and restores the real keep-inventory value across nested scopes.

The probe is a separate test mod. Its classes and metadata are excluded from production JARs. A successful result requires a fresh pass marker, the explicit `RINLIB_RUNTIME_PASS` log entry, no failure marker, and a successful process exit. These are dedicated-server API tests; they do not exercise connected clients or graphical rendering.

```sh
python3 runtime-tests/run.py --profile 1.21.1 --loader neoforge \
  --eula /path/to/an/accepted/eula.txt
```

Supply the same `JAVA_*_HOME` variables as the build matrix. Tests bind to localhost; `RINLIB_PROBE_PORT` selects a port when running independent tests concurrently. Logs and results default to `build/runtime`.

For basic readiness and clean shutdown of the default development servers:

```sh
python3 scripts/server-smoke.py
```

Their run directories must already contain an accepted `eula.txt`. Startup notices and EULA prompts do not count as readiness.

Minecraft 1.12.2 uses an installed standalone Forge server and a separately reobfuscated probe JAR:

```sh
python3 runtime-tests/run.py --profile 1.12.2 \
  --legacy-server /path/to/forge-1.12.2-server \
  --eula /path/to/an/accepted/eula.txt
```

The server directory must contain its Forge universal JAR, Minecraft server JAR and `libraries` directory. Tests copy the server JARs and reuse the installed libraries; worlds and mods are kept in the test output directory.

Local container checks accept `RINLIB_EULA_FILE` for an existing accepted EULA file. `RUN_SERVER_SMOKE=0` runs build and unit checks only.
