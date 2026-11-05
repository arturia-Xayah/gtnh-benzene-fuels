# 0.4.6 · GTNH 2.9.0 验证 / Validation

2026-11-05。21 项单元测试、984 项独立后台检查通过，服务端退出码 0。

- 结构预览：使用与 BlockRenderer 替换列表相同的 `getBlocksToPlace` 谓词，验证输入仓、维护仓、普通动力仓、UXV 64A 动力仓及两种回退外壳，并排除前后分区不允许的仓室。
- 连续发电：4 个 100 tick 无线批次，逐 tick 对照真实无线到账与 GUI 快照，批次边界没有虚假的 0 功率；缺料后正常停机。
- UXV 64A 动力仓：直接连接同级能源仓，经过原生仓室 tick，累计接收 2,748,779,069,440 EU。移除接收端后，缓存达到 137,438,953,984 EU，满仓后的多余能量正确作废；主机快照展示实际缓存和容量。
- 显示：大数字缩写、分离设置/有效上限、悬停精确数值、彩色信息。断开动力仓的“实际发送”指主机注入仓室缓存的能量，不代表下游已消费。

未启动 Minecraft 客户端。结构候选和显示数据通过后台接口验证；未声称完成客户端点击、字体布局或渲染实测。生产 JAR 不含测试夹具，Java 8 类与资源和已执行夹具对应部分逐字节一致。

[后台结果](verification/0.4.6/backend-tests.json) · [安装包验证](verification/0.4.6/distribution.json)

21 unit tests and 984 dedicated-server checks passed. Native replacement predicates, wireless tick-by-tick display continuity, real UXV 64A dynamo-to-energy-hatch transfer, disconnected buffer saturation and overflow accounting were checked. The controller now exposes real dynamo storage in its synchronized tooltip data. Production classes/assets match the executed fixture. No Minecraft client was launched; client clicks, fonts and rendering were not tested.

---

# 0.4.5 · GTNH 2.9.0 验证 / Validation

2026-11-04。20 项单元测试、967 项独立后台检查通过，服务端退出码 0。新增检查覆盖全部 43 个物品图标映射、35 个新图标与 11 种流体资源、原生动画元数据、动画帧差异、固定外壳与等级标记、透明轮廓，以及流体实际注册的仍图／流动图路径与白色染色。

新增 46 张贴图，其中 21 张为 64 帧原生动画。生产包 31 个 Java 8 类，生产类及资源与后台实测夹具一致。已有燃料和机器贴图逐字节保留。静态总览已检查，网页预览直接使用打包帧；没有启动游戏客户端，因此未进行 Minecraft 图集上传及资源包覆盖验收。

[0.4.5 后台结果](verification/0.4.5/backend-tests.json) · [0.4.5 安装包验证](verification/0.4.5/distribution.json) · [材质清单](../../preview/components/manifest.json)

20 unit tests and 967 dedicated-server checks passed. Coverage includes all 43 item mappings, new assets, animation frames and metadata, fixed cell shells/tier contacts, alpha silhouettes and actual fluid registration. The 31 Java 8 production classes and assets match the executed fixture. Existing sprites are byte-identical. The contact sheet was inspected; no Minecraft client, atlas upload or resource-pack override testing was performed.

---

# 0.4.4 · GTNH 2.9.0 验证 / Validation

2026-11-04。18 项单元测试、880 项独立后台检查通过，服务端退出码 0。

新增检查覆盖 36 种受支持燃料、900 条 NEI 参考配方（每种燃料的两侧基液有 25 种搭配）、1000:1:1 输入用量、完整精度能量、控制器配方关联、流体说明去重和双语翻译。所有配方为仅供显示的参考数据，实际发电仍由原有 100 tick 批次逻辑执行。

使用 GT 的共用流体说明入口连接 NEI 流体显示物品和 ModularUI 流体槽。此客户端钩子不在专用服务端加载。测试检查共享说明生成逻辑、配方数据及打包内容；没有启动 Minecraft 客户端，因此未实际按键验收 R/U 页面或检查客户端字体排版。

[0.4.4 后台结果](verification/0.4.4/backend-tests.json) · [0.4.4 安装包验证](verification/0.4.4/distribution.json)

18 unit tests and 880 dedicated-server checks passed. New checks cover the 900 reference recipes, 36 supported fuels, exact inputs and energy, controller association, fluid tooltip deduplication and translations. The client hook is excluded on dedicated servers. No game client was launched: actual R/U interaction and client text rendering remain unverified.

---

# 0.4.3 · GTNH 2.9.0 验证 / Validation

2026-11-03。使用官方 GTNH 2.9.0 Java 17–26 服务端及 Java 25，所有游戏逻辑检查在独立后台服务端进行；没有启动客户端或修改玩家世界。

| 检查 | 结果 |
|---|---|
| 单元测试 | 18 / 18 通过 |
| 完整整合包后台检查 | 821 / 821 通过，退出码 0 |
| 真实发电塔搭建与检测 | 15,472 格结构，仓室区域和 14 种线圈检查通过 |
| 动画资源 | 16 个 64×4096 PNG，各 64 个不同帧；原生 mcmeta 时序 |
| 循环衔接 | 首尾变化与普通相邻帧相当，最大中位步长比 1.189 |
| 玩家包 | 22 个 Java 8 生产类，无测试 Mod；生产类与 assets 和实测夹具逐字节一致 |
| 既有数据 | 配方、材料映射和说明导出与 0.4.2 基线一致 |

后台覆盖八级燃料与单元、电压之星、大数量原料扣除、原生机器阶段限制、结构和仓室位置、首燃料选择、整桶扣料及底数千分之一消耗、100 tick 发电、动力仓溢出损耗、无线记账、核心升级、保存恢复、强制结构重检和运行外壳状态。新增资源检查验证每级仍图／流动图路径、白色染色及 PNG/mcmeta 在实际 JAR 内可读取。

浏览器预览检查了八种燃料及对应单元、暂停恢复、逐帧和 2×2 平铺；它使用打包帧直接绘制。没有启动 Minecraft 验收 GPU 图集上传、资源包叠加、字体或鼠标交互，也没有本轮玩家世界迁移测试。

## 实际依赖

GregTech **5.09.54.265**、AE2 **rv3-beta-1095-GTNH**、StructureLib **1.4.47**、ModularUI2 **2.3.94-1.7.10**、GTNHLib **0.11.54**、Waila **1.19.34**。后台按实际 FML 运行版本逐一断言。

基准来源：[官方 GTNH 2.9.0 清单](https://github.com/GTNewHorizons/DreamAssemblerXXL/blob/1b62ef00ad679c51ab0ec67f8a8e42778a059ce6/releases/manifests/2.9.0.json)。

## 原始记录

- [安装包哈希与比对](verification/distribution.json)
- [821 项结果及实际依赖](verification/backend-tests.json)
- [动画检查](verification/texture-validation.json)及[资源哈希](verification/texture-manifest.json)
- [注册配方](verification/registered-recipes.json)、[燃料说明](verification/fuel-tooltips.json)、[材料身份](verification/native-material-palette.json)、[逐 tick 轨迹](verification/generator-ticks.csv)

上传记录省略作者本机绝对路径和构建日志。测试记录中的历史 L 描述沿用 GT 原始流体单位，玩家指南统一使用 mB/B；配方数量与热值没有缩放。

## English

All 18 unit tests and 821 dedicated-server checks passed on official GTNH 2.9.0. The 22 Java 8 production classes and packaged assets match the executed fixture byte-for-byte; test code is excluded. Coverage includes recipes, large input amounts, the complete tower, coil/hatch rules, batches, overflow losses, wireless accounting, saved state and all sixteen animated sprites. Browser previews use the packaged frames. No Minecraft client, GPU/resource-pack acceptance or player-world migration was performed. Published evidence omits author-specific absolute paths and local build logs.
