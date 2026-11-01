# Version support

The 2.0 target table is [support/profiles.json](../support/profiles.json).
It contains 69 exact Minecraft/loader combinations derived from consumer build configurations;
[targets.md](targets.md) lists every consumer and combination.

Every loader artifact now compiles against its own Minecraft and loader dependencies.
There are no portable/state/full tiers and no metadata-only repackaged JARs.
Each row declares Minecraft, loader, Java, pinned dependencies and the complete source-directory list.
The common build script reads this list directly.

The complete `dev.rinchan.rinlib.api` contract covers 17 data-component-era Minecraft versions:
1.20.6, 1.21 and 1.21.1–1.21.11, 26.1, 26.1.1, 26.1.2 and 26.2.
All 44 matching loader targets select these adapters, including Forge where configured.
Each MC version has its own implementation directory. Source signature checks run with
`python3 tools/check_adapter_api.py`; compilation is a separate check.
The other 25 targets retain the existing classes needed by their consumers. They do not
supply the full new API: their older item NBT model and, in legacy Forge, renamed MC types
need a separate serialization/API design.

Existing mod-specific classes remain until consumers can migrate them in their own repositories.
See [migration-2.0.md](migration-2.0.md) for per-class destinations and exact consumer edits.
The old published API fixture remains historical compatibility evidence; it is not the 2.0
target list or a requirement to run the entire old matrix during a local change.
