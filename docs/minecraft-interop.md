# Minecraft integration

Stable entrypoints are in `dev.rinchan.rinlib.api` for 17 data-component-era versions:
1.20.6, 1.21 and 1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2 and 26.2.
They are selected for all 44 matching loader targets in the consumer-derived table, including Forge where configured.

- `ItemProperties.of(String id)` validates a resource ID and initializes the item properties
  with its registry key on versions requiring one.
- `PlayerContext.name(player)` hides the Authlib name-accessor change.
  `functionSource(player)` produces a permission-level-two source with output suppressed.
- `MinecraftData.of(registries)` provides registry-aware component JSON and item serialization,
  copied custom data and item name/lore decoration. Minecraft codecs define the data format.
- `MobEffectState.add(player, String id, ticks, amplifier)` resolves an effect and adds it;
  malformed or missing IDs return false on either loader.
- `PackFormats.serverDataMajor()` / `serverDataMinor()` expose the current data-pack format.
  `serverDataMetadata(description)` encodes the appropriate `pack_format` or min/max format JSON.
- `FunctionExecution.executeTag(player, String id)` synchronously executes a function tag and
  returns the number of functions invoked. Missing tags are empty. Functions run with the
  player's position and a suppressed-output permission-level-two source.
  `isolated(server, supplier)` exposes the same isolation for custom callbacks.

Function execution must run on the server thread. An existing command context is temporarily
removed and restored in `finally`, including when the callback throws. This does not prevent
recursive application callbacks; callers keep their own reentry guard.

Old `dev.rinchan.rinlib.minecraft` entrypoints remain deprecated forwarding adapters during
consumer migration. They are not the stable API namespace. Structure resources, lifecycle,
prepared packs and projection helpers remain in place this round; their planned owning mods
and migration edits are documented in [migration-2.0.md](migration-2.0.md).
