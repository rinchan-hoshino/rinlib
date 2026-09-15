# Changelog

## 1.0.2

- Build all release profiles from one checkout and shared sources, without merging old release JARs.
- Keep existing public APIs and separate only Minecraft and loader-specific adapters.
- Plan payments in one pass and avoid overflow when inventory value exceeds `long`.
- Correct snapshot version metadata for Fabric 22w24a.
- Share Gradle test conventions and remove an unused Fabric API dependency from the library build.
- Require real server readiness and clean shutdown in smoke checks; add in-server API assertions.

## 1.0.1

- Make death-scoped `keepInventory` projection own the complete gamerule read, avoiding callback-cancellation ordering while preserving the world's stored rule.

## 1.0.0

- Formalizes RinLib as the stable shared contract for RinChan mods.
- Preserves Minecraft-bound damage and state bridges on the three release anchors.
- Includes denomination payment planning in every profile.
- Builds formal anchors from exact hash-verified prior bridge artifacts plus current portable contracts.

## 0.4.0

- Added the portable `DenominatedPayment` planner.
- Added deterministic Fabric, Forge and NeoForge profile assembly for Paid Waystones targets.
- Added focused tests for exact payment, mixed denominations, change, insufficient funds, zero cost and overflow-safe totals.
