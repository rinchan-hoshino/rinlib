# RinLib 2.0 migration

本轮只改 RinLib。以下消费者修改是后续操作清单，未修改任何消费者仓库。
保留模组专属类及其现有签名；旧 `minecraft` 包的跨版本入口标记废弃并转发到 `api`。

## 范围与取舍

- 构建目标采用 [targets.md](targets.md) 中的真实并集：69 个精确版本/加载器组合。删除 portable 档，不删除仍有消费者的旧版本。另一方案是直接砍掉这些版本，代价是 shared-lootr、付费传送石等无法迁移；本轮不这样做。
- 完整新 API 当前覆盖 **1.20.6、1.21 及 1.21.1–1.21.11、26.1、26.1.1、26.1.2、26.2 的全部已配置加载器，共 17 个版本、44 个目标**，每个 MC 版本一个 `adapters/<minecraft>/dev/rinchan/rinlib/api` 实现目录。其余 25 个目标保留消费者原来需要的类，**不承诺完整新 API**。1.20.4 及更早版本仍使用旧 NBT 物品模型；旧 Forge 1.12/1.16 的 MC 类型名也不同。该断层需要单独设计序列化和数据写入语义，本轮没有用空实现冒充支持。
- 保留旧入口的理由是消费者不在本轮修改范围内；旧入口只转发，业务实现归新 API。后续消费者完成迁移后，才删除这些入口。
- 版本、加载器、Java、依赖版本及源码目录只有 `support/profiles.json` 一份表；不再根据 MC 版本字符串拼装源码。增加实现必须同时补表和签名检查。

## 新稳定 API

包名统一为 `dev.rinchan.rinlib.api`；ID 输入统一为 `String`。MC 对象使用这 17 个版本中同名的 `Item.Properties`、`ItemStack`、`ServerPlayer`、`MinecraftServer`、`CommandSourceStack`、`HolderLookup.Provider`、`Component`、`CompoundTag`。这是 Mojang 命名下的源码接口保证；Fabric 发布 JAR 仍由 Loom 正常重映射。

| 类 | 消费者修改 |
| --- | --- |
| `ItemProperties` | kill-confirmed `common/src/main/java/dev/rinchan/killconfirmed/KillConfirmed.java:4` 的 import 改为 `api.ItemProperties`；`:25` 的 `ItemProperties.of(DOG_TAG_ID)` 改为 `ItemProperties.of(DOG_TAG_ID.toString())`。消费者不再为此方法替换 ResourceLocation/Identifier 类型。 |
| `PlayerContext` | `DogTagLifecycle.java`、`DatapackPlaceholderBridge.java` 的 `minecraft.PlayerContext` import 改为 `api.PlayerContext`；`name(player)`、`functionSource(player)` 调用不变。后者仍为权限等级 2、抑制输出，不继承玩家 OP 等级。 |
| `MinecraftData` | `DogTagItemFactory.java`、`DogTagLifecycle.java`、`DogTagSnapshotCodec.java`、`DatapackPlaceholderBridge.java` 的 `minecraft.MinecraftData` import 改为 `api.MinecraftData`；工厂、组件 JSON、物品保存/读取、自定义数据和名称/lore 的调用不变。返回的 customData 是副本，设置时也复制。 |
| `FunctionExecution` | `DatapackPlaceholderBridge.java:12` 改 import。保留 `isolated(server, supplier)`；其函数标签循环可整体替换成 `FunctionExecution.executeTag(player, CALLBACK_TAG.toString())`。新方法同步执行标签，返回执行函数数量；缺失标签返回 0，异常向上传递，必须在服务器线程调用。 |
| `MobEffectState` | 若调用旧 `minecraft.MobEffectState.add`，改 import 为 `api.MobEffectState`；`add(player, stringId, ticks, amplifier)` 不变。非法或不存在的 ID 返回 false。现在两个加载器都有此入口。 |
| `PackFormats` | structure-dfu-cache 的 `StructureCacheBootstrap.java:151` 可将 `StructureResources.packMetadata()` 改为 `PackFormats.serverDataMetadata("Prepared structure resources")` 并新增 API import。1.20.1 暂用旧入口；该目标尚无完整新 API。旧格式写 `pack_format`，新格式写当前 major/minor 的 `min_format`、`max_format`，description 由 JSON 编码器转义。 |

先更新消费者 `rinlib_version` 到 `2.0.0`，再更新导入。旧 `ItemProperties.of(ResourceLocation/Identifier)` 保留在废弃入口；新包没有这两种参数类型。注册物品、mixin 注入点及加载器事件仍由模组自己的加载器适配层负责。

## 模组专属类的后续归属（本轮均保留）

| 现有类/文件 | 搬去哪个模组 | 消费者具体改动 |
| --- | --- | --- |
| `payment/DenominatedPayment`（含 `Stack`、`Removal`、`Plan`） | waystones-menu-fix：`dev.rinchan.waystoneemeralds.payment` | 将实现及 `DenominatedPayment*Test` 搬进该模组。`common/src/main/java/dev/rinchan/waystoneemeralds/payment/ItemPayment.java:3` 删除外部 import，`:37,43,46,47` 的同包类调用不变。保持单位值、零钱、溢出与计划不可变性测试。paper-plane 当前检出没有调用此类，不向它复制一份。 |
| `state/SharedOwnerState` | shared-lootr：`dev.rinchan.sharedlootr.state` | 将源码搬入该模组的 shared/common 源集；所有 `import dev.rinchan.rinlib.state.SharedOwnerState` 改为 `dev.rinchan.sharedlootr.state.SharedOwnerState`。例如 `adapters/chest-data-neoforge-1.20.4/neoforge/dev/rinchan/sharedlootr/mixin/NeoForgeMinecartOpenProjectionMixin.java:3`、`adapters/chest-data-1.20.1/fabric/dev/rinchan/sharedlootr/integration/jade/FabricSharedLootrJadePlugin.java:3`。所有 `OWNER/get/put/remove/contains` 调用不变；UUID 必须保持 `new UUID(0L, 1L)`，不能重建玩家存档 key。 |
| `minecraft/KeepInventoryProjection` | keep-my-sword：`dev.rinchan.keepmysword.minecraft`，连同需要投影 keepInventory 的 mixin | 搬实现、测试和下述 `GameRulesMixin`；改 mixin 内对该类的 import。当前 keep-my-sword 主源码没有直接调用它，现存调用在 RinLib runtime probes；不能在消费者中凭空加调用。 |
| `mixin/GameRulesMixin`（1.21 / 26） | keep-my-sword 的版本适配目录 | 迁移后改包声明和 mixin JSON 的 `package`；由该模组注册配置，删除 RinLib 对该配置的声明。该步骤必须与 `KeepInventoryProjection` 同批，避免失去真实 game rule 投影。 |
| `item/DamageState`（shared / 1.16） | keep-my-sword：`dev.rinchan.keepmysword.item` | `KeepMySword.java:3` 改 import；`item/SealedItems.java:30` 的全限定名改为本地类。broken 语义和 GUI/模型着色常量属于此模组，`isBroken`、`clampDamage` 行为保持。旧 1.16 实现留在其对应版本目录。 |
| `minecraft/StructureLifecycle`（含 `Preparation`） | structure-dfu-cache-native-fix / 当前源码名 structure-dfu-cache：`dev.rinchan.structuredfucache.minecraft` | `src/main/java/dev/rinchan/structuredfucache/StructureDfuCache.java:4`、`cache/StructureCacheBootstrap.java:18` 改 import；`:14` 的 `register`、bootstrap `implements Preparation` 和 prepare/close 方法不变。保留每个资源源独立实例、停止时关闭和 OP 登录通知。 |
| `minecraft/StructureResources` | 同上，`dev.rinchan.structuredfucache.minecraft` | `cache/StructureCacheBootstrap.java:19`、`cache/StructureCacheBuilder.java:35` 和测试内的 import 改包名；`:80,92,134` 的 list/overlay/resourcePath、builder `:307` 的 readCompressed 不变；pack metadata 按上表逐版本改用稳定 API。 |
| `minecraft/StructurePlatform`（1.20 / 1.21 / 26） | 该模组各 MC 版本适配目录 | 非公开类，无外部调用修改。与 `StructureResources` 一起改包；保留旧 `structures` 与新 `structure` 目录、NBT 预算和 vanilla pack 构造差异。现代 pack metadata 继续委托 `api.PackFormats`。 |
| `minecraft/StructurePreparedPack` | 该模组 shared 源集 | 非公开类，无外部调用修改。与子类一起改包；保留 delegated resource 的原始拥有者，关闭时不能关闭 Minecraft 的原始资源包。 |
| `minecraft/StructureHybridPack`（1.20 / 1.21 / 26） | 该模组各 MC 版本适配目录 | 非公开类，无外部调用修改；与 `StructurePreparedPack` 一起改包，保留磁盘/回退资源的合并和 namespace 行为。 |
| `mixin/structure/TemplateResourcesMixin`（legacy-hooks / 26） | 该模组各 MC 版本适配目录 | 改包、改 `StructureLifecycle` import，更新该模组 mixin JSON 的包名及类名列表；Forge 一起迁移 refmap 名称与 Gradle mixin 配置。 |
| `mixin/structure/ServerStopMixin` | 该模组 shared mixin 源集 | 改包及 `StructureLifecycle` import，在该模组 mixin JSON 注册；`closeAll()` 的注入时机保持。 |
| `mixin/structure/OperatorLoginMixin`（1.20 / 1.21 / 26） | 该模组各 MC 版本适配目录 | 改包及 `StructureLifecycle` import，在该模组 mixin JSON 注册；只发给操作员，保留版本对应的权限检查。 |
| `state/StickyBoolean` | 原调用位置内联，无需搬类 | 已标 `@Deprecated`。`StickyBoolean.next(current, update)` 改为 `current || update`，随后删 import。当前生产消费者检出未发现调用，RinLib 自测仍保留。 |
| `state/ReentrantFlag`（含 `Scope`） | kill-confirmed 的 `portable` 包；如 paper-plane 旧发布线还调用，可独立搬到该模组 common | kill-confirmed `DatapackPlaceholderBridge.java:9` 改 import，`enter/isSet/try-with-resources` 不变。它有线程归属、嵌套深度和重复关闭约束，不是一行布尔工具，不做机械内联。 |
| `RinLib` | 暂保留加载器入口用的 mod id 常量 | 无消费者业务调用。后续只在入口直接使用固定 mod id 后删除；不把空门面作为新 API。 |

这里的路径和行号以 [targets.md](targets.md) 的消费者快照为准；不是宣称只改一条 import 就完成跨版本移植。Structure 类及 mixin 必须整组迁移，搬动后的服务器初始化、reload、关闭和回退语义需要由该模组自己的测试验证。

## 检查方式

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
python3 tools/check_adapter_api.py
python3 -m unittest discover -s tests -p 'test_*.py'
./gradlew :common:test :fabric:build :neoforge:build
./gradlew -p profiles -Pprofile=1.20.1 -Ploader=fabric build
```

签名脚本比较所有配置中 `adapters/<minecraft>` 目录的公开类型和成员，解析 import、忽略参数名和方法体，并拒绝新包中公开的 ResourceLocation/Identifier。它不替代编译；版本实现须另外针对对应 MC 编译。旧目标扩展稳定 API 时，也必须加入同样的版本目录和完整签名，不能用空实现混过检查。
