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
