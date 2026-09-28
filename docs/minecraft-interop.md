# Minecraft integration

The full Fabric and NeoForge profiles for 1.21.1, 26.1.2 and 26.2 expose these helpers starting with RinLib 1.1.0:

- `ItemProperties.of(id)`: initialize item properties with the registry ID required by newer Minecraft versions.
- `MinecraftData.of(registries)`: registry-aware component JSON, item NBT, custom item data, display names and lore. Item encoding uses Minecraft's own codec.
- `PlayerContext.name(player)`: the profile name across Authlib API changes.
- `PlayerContext.functionSource(player)`: a suppressed-output, permission-level-two command source at the player.
- `FunctionExecution.isolated(server, action)`: run synchronous function callbacks independently of an already executing command queue. Use it on the server thread. The previous queue is restored in `finally`, including when the action throws.

`isolated` does not guard recursive application callbacks; callers that prohibit reentry should use `ReentrantFlag`. It is intended for callbacks whose output must be read before returning to the surrounding command. Ordinary queued command execution should keep using Minecraft's normal function API.

These APIs are exercised by Kill Confirmed's real-server regression suite on every supported loader/version pair.
