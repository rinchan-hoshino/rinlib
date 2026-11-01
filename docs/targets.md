# RinLib 2.0 targets

盘点日期：2026-11-02。只读取消费者检出，不修改、构建或发布消费者。

## 结论与范围

消费者构建配置的并集为 **69 个 (Minecraft, loader)**。原有 67 个 profile 均有真实消费者，因此删除目标 **0** 个；原先 1.21.3 包含的 1.21.2 改为两个独立构建，新增 2 个。删除 35 条 portable profile（覆盖 37 个兼容组合）的元数据换壳构建，全部保留目标改为针对真实 Minecraft/loader 编译。没有将 Lootr 上游支持矩阵、README 兼容范围或历史测试清单当成发布目标。

旧目标暂时仍供应本轮要求保留的模组专属类；完整稳定 API 覆盖数据组件时代的 17 个 MC 版本、44 个目标（1.20.6、1.21 及 1.21.1–1.21.11、26.1、26.1.1、26.1.2、26.2）。其余 25 个旧目标不能据此宣称具备完整新 API。详见 migration-2.0.md。

## 消费者证据

- **shared-lootr**（`dd36b92`，56 个目标）：[support/release-targets.json](/Users/rin/Documents/Codex/2026-10-08/shared-lootr-lootr-api-release/work/shared-lootr/support/release-targets.json)；检出 `/Users/rin/Documents/Codex/2026-10-08/shared-lootr-lootr-api-release/work/shared-lootr`。
- **waystones-menu-fix**（`de0ad19`，41 个目标）：[support/profiles.json](/Users/rin/Documents/Codex/2026-10-04/public-mod-feedback/waystones-menu-fix/support/profiles.json)；检出 `/Users/rin/Documents/Codex/2026-10-04/public-mod-feedback/waystones-menu-fix`。
- **paper-plane**（`5fe0dcd`，17 个目标）：[matrix.json](/Users/rin/Documents/Codex/2026-10-27/new-chat-3/work/paper-plane/matrix.json)；检出 `/Users/rin/Documents/Codex/2026-10-27/new-chat-3/work/paper-plane`。
- **keep-my-sword**（`da179d4`，6 个目标）：[profiles.json](/Users/rin/Documents/Codex/2026-10-27/new-chat-3/work/keep-my-sword/profiles.json)；检出 `/Users/rin/Documents/Codex/2026-10-27/new-chat-3/work/keep-my-sword`。
- **kill-confirmed**（`40cb6c2`，6 个目标）：[profiles.json](/Users/rin/Documents/Codex/2026-10-27/new-chat-3/work/kill-confirmed/profiles.json)；检出 `/Users/rin/Documents/Codex/2026-10-27/new-chat-3/work/kill-confirmed`。
- **structure-dfu-cache**（`9b6e100`，8 个目标）：[profiles.json](/Users/rin/Documents/Codex/2026-10-05/dfu-tps-dfu-tps-x20-requirements/code/structure-dfu-cache/profiles.json)；检出 `/Users/rin/Documents/Codex/2026-10-05/dfu-tps-dfu-tps-x20-requirements/code/structure-dfu-cache`。
- **let-your-friend-drink**（`d493835`，2 个目标）：[gradle.properties + settings.gradle](/Users/rin/Documents/Codex/2026-10-25/vg-2/work/native-public-sources/let-your-friend-drink/gradle.properties)；检出 `/Users/rin/Documents/Codex/2026-10-25/vg-2/work/native-public-sources/let-your-friend-drink`。
- **skill-respec-pill**（`d91d355c8`，2 个目标）：[gradle.properties + settings.gradle](/Users/rin/Documents/Codex/2026-10-08/1-season-01-2-maven-3/work/mod-update-discovery-20261011/mod-sources/skill-respec-pill/gradle.properties)；检出 `/Users/rin/Documents/Codex/2026-10-08/1-season-01-2-maven-3/work/mod-update-discovery-20261011/mod-sources/skill-respec-pill`。

structure-dfu-cache-native-fix 的另一个检出位于 `2026-10-21/mc-mine-and-slash/outputs/structure-dfu-cache-native-fix`；其 `profiles.json` 与当前主工作检出相同的八个目标。重复检出、导出的 uploaded-source 和 runtime-tests 不重复计数。`wmf-skills` 的 RinLib 声明只在本地探针配置里，没有新增正式发布目标。

## 实际构建/发布组合

本轮构建 27 个目标；其余 42 个未构建，不视为编译失败。没有运行完整矩阵 CI。

| Minecraft | loader | 消费者 | 本轮构建 |
| --- | --- | --- | --- |
| 1.12.2 | forge | shared-lootr | 未构建 |
| 1.16.5 | forge | shared-lootr | 未构建 |
| 1.17.1 | forge | shared-lootr | 未构建 |
| 1.18 | fabric | shared-lootr | 未构建 |
| 1.18 | forge | shared-lootr | 未构建 |
| 1.18.1 | fabric | shared-lootr | 未构建 |
| 1.18.1 | forge | shared-lootr | 未构建 |
| 1.18.2 | fabric | shared-lootr | 未构建 |
| 1.18.2 | forge | shared-lootr | 未构建 |
| 1.19 | fabric | shared-lootr | 未构建 |
| 1.19 | forge | shared-lootr | 未构建 |
| 1.19.1 | fabric | shared-lootr | 未构建 |
| 1.19.1 | forge | shared-lootr | 未构建 |
| 1.19.2 | fabric | shared-lootr, paper-plane | 未构建 |
| 1.19.2 | forge | shared-lootr, paper-plane | 未构建 |
| 1.19.3 | fabric | shared-lootr | 未构建 |
| 1.19.4 | fabric | shared-lootr | 未构建 |
| 1.20 | fabric | shared-lootr | 未构建 |
| 1.20 | forge | shared-lootr | 未构建 |
| 1.20.1 | fabric | shared-lootr, paper-plane, structure-dfu-cache | 通过 |
| 1.20.1 | forge | shared-lootr, paper-plane, structure-dfu-cache | 通过 |
| 1.20.4 | fabric | shared-lootr, waystones-menu-fix, paper-plane | 通过 |
| 1.20.4 | forge | waystones-menu-fix, paper-plane | 未构建 |
| 1.20.4 | neoforge | shared-lootr, waystones-menu-fix, paper-plane | 通过 |
| 1.20.6 | fabric | waystones-menu-fix, paper-plane | 通过 |
| 1.20.6 | forge | waystones-menu-fix | 未构建 |
| 1.20.6 | neoforge | waystones-menu-fix, paper-plane | 通过 |
| 1.21 | fabric | shared-lootr, waystones-menu-fix, paper-plane | 未构建 |
| 1.21 | forge | waystones-menu-fix | 未构建 |
| 1.21 | neoforge | shared-lootr, waystones-menu-fix, paper-plane | 通过 |
| 1.21.1 | fabric | shared-lootr, waystones-menu-fix, paper-plane, keep-my-sword, kill-confirmed, structure-dfu-cache, let-your-friend-drink, skill-respec-pill | 通过 |
| 1.21.1 | forge | waystones-menu-fix | 通过 |
| 1.21.1 | neoforge | shared-lootr, waystones-menu-fix, paper-plane, keep-my-sword, kill-confirmed, structure-dfu-cache, let-your-friend-drink, skill-respec-pill | 通过 |
| 1.21.10 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.10 | forge | waystones-menu-fix | 未构建 |
| 1.21.10 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.11 | fabric | shared-lootr, waystones-menu-fix, paper-plane | 通过 |
| 1.21.11 | forge | waystones-menu-fix | 未构建 |
| 1.21.11 | neoforge | shared-lootr, waystones-menu-fix, paper-plane | 未构建 |
| 1.21.2 | fabric | shared-lootr | 通过 |
| 1.21.2 | neoforge | shared-lootr | 通过 |
| 1.21.3 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.3 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.4 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.4 | forge | waystones-menu-fix | 未构建 |
| 1.21.4 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.5 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.5 | forge | waystones-menu-fix | 未构建 |
| 1.21.5 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.6 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.6 | forge | waystones-menu-fix | 未构建 |
| 1.21.6 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.7 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.7 | forge | waystones-menu-fix | 未构建 |
| 1.21.7 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.8 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.8 | forge | waystones-menu-fix | 未构建 |
| 1.21.8 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 1.21.9 | fabric | shared-lootr, waystones-menu-fix | 未构建 |
| 1.21.9 | neoforge | shared-lootr, waystones-menu-fix | 通过 |
| 22w24a | fabric | shared-lootr | 未构建 |
| 26.1 | fabric | shared-lootr | 通过 |
| 26.1 | neoforge | shared-lootr | 未构建 |
| 26.1.1 | fabric | shared-lootr | 通过 |
| 26.1.1 | neoforge | shared-lootr | 未构建 |
| 26.1.2 | fabric | shared-lootr, waystones-menu-fix, paper-plane, keep-my-sword, kill-confirmed, structure-dfu-cache | 通过 |
| 26.1.2 | neoforge | shared-lootr, waystones-menu-fix, paper-plane, keep-my-sword, kill-confirmed, structure-dfu-cache | 通过 |
| 26.2 | fabric | shared-lootr, waystones-menu-fix, keep-my-sword, kill-confirmed, structure-dfu-cache | 通过 |
| 26.2 | neoforge | shared-lootr, waystones-menu-fix, keep-my-sword, kill-confirmed, structure-dfu-cache | 通过 |
