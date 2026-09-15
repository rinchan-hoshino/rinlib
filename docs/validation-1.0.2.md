# RinLib 1.0.2 validation

## Results

- 67 production artifacts built from this checkout, covering 32 Minecraft versions and 69 Minecraft/loader combinations.
- All 67 artifacts passed published public-API compatibility, bytecode, metadata, license and test-isolation checks.
- 35 local dedicated-server cases passed across all 32 Minecraft versions.
- 15 Java unit tests passed on Java 17 and 21, including 2,000 generated payment examples; 9 Python checks passed.
- Default Fabric and NeoForge development servers reached readiness and stopped cleanly.

Minecraft 1.12.2 was tested with the production JAR on a standalone Forge server. Other runtime cases used development dedicated servers. Each game version has a runtime case; every loader combination was built and checked, but not every loader combination was launched. Connected clients and graphical rendering were not tested.

## Runtime coverage

| Minecraft | Loader | Assertions | Result |
| --- | --- | ---: | --- |
| 1.12.2 | forge | 6 | Pass |
| 1.16.5 | forge | 11 | Pass |
| 1.17.1 | forge | 11 | Pass |
| 1.18 | fabric | 11 | Pass |
| 1.18.1 | fabric | 11 | Pass |
| 1.18.2 | fabric | 11 | Pass |
| 1.19 | fabric | 11 | Pass |
| 1.19.1 | fabric | 11 | Pass |
| 1.19.2 | fabric | 11 | Pass |
| 1.19.3 | fabric | 11 | Pass |
| 1.19.4 | fabric | 11 | Pass |
| 1.20 | fabric | 11 | Pass |
| 1.20.1 | fabric | 11 | Pass |
| 1.20.4 | fabric | 10 | Pass |
| 1.20.6 | fabric | 10 | Pass |
| 1.21 | fabric | 10 | Pass |
| 1.21.1 | fabric | 15 | Pass |
| 1.21.1 | neoforge | 24 | Pass |
| 1.21.10 | fabric | 10 | Pass |
| 1.21.11 | fabric | 10 | Pass |
| 1.21.2 | fabric | 10 | Pass |
| 1.21.3 | fabric | 10 | Pass |
| 1.21.4 | fabric | 10 | Pass |
| 1.21.5 | fabric | 10 | Pass |
| 1.21.6 | fabric | 10 | Pass |
| 1.21.7 | fabric | 10 | Pass |
| 1.21.8 | fabric | 10 | Pass |
| 1.21.9 | fabric | 10 | Pass |
| 22w24a | fabric | 11 | Pass |
| 26.1 | fabric | 11 | Pass |
| 26.1.1 | fabric | 11 | Pass |
| 26.1.2 | fabric | 15 | Pass |
| 26.1.2 | neoforge | 24 | Pass |
| 26.2 | fabric | 15 | Pass |
| 26.2 | neoforge | 24 | Pass |

## Regressions covered

- Payment planning remains ordered and immutable, preserves value, and handles inventories whose total value exceeds `long`.
- Shared-owner map operations retain UUID `00000000-0000-0000-0000-000000000001` and preserve actor-specific entries.
- Item durability adapters run against real Minecraft item stacks.
- Full NeoForge profiles apply effects to server-side players and project the real keep-inventory game rule only while scoped.
- Fabric 22w24a uses the loader-normalized Minecraft version `1.19.1-alpha.22.24.a`; the corrected artifact starts successfully.
- Readiness checks reject startup notices, EULA-only exits and crashes after readiness.

Artifact and log hashes are listed in [the machine-readable report](validation-1.0.2.json).
