# Version support

[`support/profiles.json`](../support/profiles.json) is the release matrix. It tracks the latest published profile for each supported Minecraft and loader combination, including its existing public API. There are 67 artifacts covering 32 Minecraft versions and 69 Minecraft/loader combinations; the 1.21.3 Fabric and NeoForge artifacts also support 1.21.2.

## Shared implementations

| Profile | APIs | Source |
| --- | --- | --- |
| `legacy` | Shared owner and sticky boolean | Common state code, legacy Forge entrypoint |
| `state` | Shared owner, sticky boolean and item durability | Common state code, Minecraft item adapter, loader entrypoint |
| `portable` | Shared owner, sticky boolean, scoped flags and payment planning | Common Java code, compiled to Java 17 |
| `full` | Portable APIs and item durability; NeoForge also supplies effect and game-rule bridges | Common Java code, Minecraft adapters, loader entrypoint |

The full profiles are Minecraft 1.21.1, 26.1.2 and 26.2 on Fabric and NeoForge. API availability is preserved from the latest published artifact for each profile. `KeepInventoryProjection` provides a scope; the actual game-rule projection mixin is included in full NeoForge profiles.

Shared map identity remains `new UUID(0L, 1L)`. `SharedOwnerState.put` returns the inserted value. Existing actor-specific entries are left untouched.

## Builds

Every artifact is compiled from this repository. Portable profiles reuse one Java 17 JAR with loader-specific metadata. Minecraft-bound profiles compile the same common sources against their selected Minecraft and loader dependencies; old release JARs are not build inputs.

Gradle outputs and project caches are isolated per Minecraft/loader profile. The legacy Forge build uses Gradle 4.10.3 and Java 8; current Forge profiles use Gradle 8.10.2; Fabric and NeoForge profiles use Gradle 9.2.1. Gradle launcher and target Java versions may differ.

Published API fixtures are stored in `tests/fixtures/public-api.json`. `tests/verify_artifacts.py` checks public binary interfaces, bytecode versions, metadata references, license inclusion and absence of runtime test classes in production JARs.


## Structure adapter (1.2.0)

Profiles marked `structure_api` also provide `StructureResources` and
`StructureLifecycle`: 1.20.1 Fabric/Forge and 1.21.1, 26.1.2, 26.2 Fabric/NeoForge.
The flag augments the profile's existing APIs. Other profiles keep their previous
source selection and API surface.
