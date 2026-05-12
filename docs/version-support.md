# Version support

RinLib is a shared library for RinChan Minecraft mods. Each supported Minecraft version has its own release line because loader APIs, Gradle plugins, mappings, and Java requirements change across eras.

## Target matrix

| Minecraft | Forge | NeoForge | Fabric | Java | Status |
| --- | --- | --- | --- | --- | --- |
| 1.7.10 | Forge `10.13.4.1614` | Not applicable | Not applicable | 8 | Target |
| 1.12.2 | Forge `14.23.5.2864` | Not applicable | Not applicable | 8 | Target |
| 1.16.5 | Forge `36.2.42` | Not applicable | Fabric API `0.42.0+1.16` | 8 | Target |
| 1.20.1 | Forge `47.4.20` | NeoForge `47.1.106` | Fabric API `0.92.9+1.20.1` | 17 | Implemented on this branch |
| 1.21.1 | Not targeted | NeoForge `21.1.229` | Fabric API `0.116.12+1.21.1` | 21 | Implemented on `main` |
| 26.1.x | Not targeted | NeoForge `26.1.2.48-beta` | Fabric API `0.148.2+26.1.2` | 21 | Target |

Forge is intentionally capped at Minecraft 1.20.1. NeoForge starts at Minecraft 1.20.1. Fabric starts at Minecraft 1.16.5.

## Release-line rule

Use one repository, but keep incompatible Minecraft eras isolated by release line. Share source only when the API contract is actually stable for that era.

Recommended source sharing:

- `shared-modern`: Minecraft 1.20.1, 1.21.1, and 26.1.x where the same Mojang-mapped concepts still compile.
- `shared-1.16`: Minecraft 1.16.5-specific shared behavior for Forge and Fabric.
- `legacy-forge`: Minecraft 1.7.10 and 1.12.2 ports, with shared behavior kept small because package names, mappings, and lifecycle APIs differ heavily.

Do not force one Java source set across all versions if that creates brittle compatibility code. The stable contract is the behavior; loader and Minecraft-version glue may differ.
