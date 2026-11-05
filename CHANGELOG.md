# 更新日志 / Changelog

## 0.5.0-alpha · GTNH 2.9.0 · 2026-11-05

- 新增原生 BetterQuesting 双语章节「苯向星辰」：46 个新任务、7 个原版任务引用，涵盖 LuV～UXV 燃料、活化剂与催化剂、UV 发电塔工程、科技／魔法 Mk1～Mk5 说明，以及 LV～UXV 电压之星。
- 沿用原版检测／勾选任务、可选备料清单和一次性职业硬币奖励；燃料与星星检测不消耗材料。尚无生存配方的材料仅作说明，不提交、不奖励，不阻挡燃料主线。
- JAR 自动接入新世界与旧存档任务书，保留原版任务、已有进度和领奖状态；支持原版默认任务重载。增加配置开关、固定独立 UUID 和中英布局预览。
- Added the bilingual native BetterQuesting chapter **Benzene Beyond the Horizon**: 46 quests plus seven original references, non-consuming milestones, one-time profession coins, generator engineering and reference-only pending upgrades. Installs additively into new and existing worlds, preserving completion and claim history across reloads.

21 项单元测试、1,251 项后台检查通过；未启动 Minecraft 客户端。 / 21 unit tests and 1,251 dedicated-server checks passed; no Minecraft client launched.

[章节说明 / Chapter guide](docs/features/QUESTS.md) · [验证 / Validation](docs/development/VALIDATION.md)

## 0.4.5-alpha · GTNH 2.9.0 · 2026-11-04

- 补齐 35 种物品、11 种流体的独立材质：活化剂、量子催化剂、十三种之星、十种指数核心、十种底数单元及其流体、UV 活化液。
- 统一像素金属外框和明暗；科技侧蓝白芯片、魔法侧紫色晶体；等级由颜色和几何标识区分。流体与底数单元使用原生 64 帧动画。
- 移除末影之眼、下界之星、水桶和烈焰粉的占位引用；白色注册染色避免二次染色。保留原有燃料贴图及所有配方、ID 和发电参数。
- Added dedicated artwork for 35 items and 11 fluids. Consistent pixel-metal frames, blue-white technology chips, purple magic crystals and tier markings replace vanilla placeholders. Fluids and base cells use native 64-frame animation. Existing fuel art and gameplay are unchanged.

20 项单元测试及 967 项后台检查通过；未启动客户端。 / Passed 20 unit tests and 967 server checks; no game client launched.

[材质与预览 / Artwork and previews](design/component-textures/README.md)

## 0.4.4-alpha · GTNH 2.9.0 · 2026-11-04

### 中文

- 燃料流体图标和 ModularUI 流体槽现在显示等级、热值、燃料当量及彩色斜体描述；单元容量说明只出现在单元上。
- 新增共振发电塔 NEI 发电配方页：主机按 R／U、燃料和底数流体（含单元）按 U 查询。覆盖全部受支持燃料及两侧 25 种底数搭配，列出 5 秒用量、每批能量和平均功率；页面采用默认指数 1，保留高阶燃料完整数值。
- 补齐发电塔中英文使用介绍及 Shift 结构说明。
- 燃料热值、实际发电逻辑、配方用量、结构和注册 ID 不变。

### English

- Fuel displays and ModularUI fluid tanks now show localized tier, heat, reference-fuel equivalence and styled flavor text. Fixed cell capacity remains cell-only.
- Added a dedicated generation page: R/U on the controller and U on fuel/base fluids or cells. Covers supported fuels and all 25 base-fluid combinations, showing 5-second inputs, batch energy and mean power at exponent 1 with full-precision high-tier values.
- Localized the controller description and Shift structure page.
- Fuel heat, runtime generation, recipe quantities, structure and registry IDs are unchanged.

Validation / 验证：18 项单元测试、880 项后台检查通过；未启动客户端。[记录 / Evidence](docs/development/VALIDATION.md)

## 0.4.3-alpha · GTNH 2.9.0 · 2026-11-03

首个公开发布版本，包含此前本地开发的完整内容。

### 中文

- 八级 LuV～UXV 苯燃料及其单元新增独立动画：64×64、64 帧循环，融合各级配方原料的视觉特征。
- 流体和单元内部使用原生纹理动画，单元金属外壳保持静止；取消二次染色，无需额外动画模组。
- 包含 33×33×129 共振发电塔、1648 个同级线圈控制的并行、每批 100 tick 发电、有线／无线输出及科技／魔法两条升级路线。
- 包含八级燃料升级配方、十三种电压之星、十种底数流体和十种指数核心；主机、外壳和增益材料的生存配方待定。
- 面向 GTNH 2.9.0 正式版更新依赖。燃料热值、配方用量、结构、注册 ID 及发电逻辑保持原有设定。
- 重整中英文 README、使用指南、素材说明和开发记录；文档统一使用 mB / B 标注，未改动配方原始数量。

验证：18 项单元测试、821 项独立后台检查通过。未启动游戏客户端。[详细记录](docs/development/VALIDATION.md)

### English

- Individual animated textures for all eight LuV–UXV fuels and their cells: 64×64, 64-frame native loops with ingredient-inspired motifs.
- Static metal cell shells with flowing interiors; full-color sprites avoid double tinting. No animation mod is required.
- Includes the 33×33×129 Resonance Fuel Generator, 1648 same-tier coils, parallel limits, 100-tick generation batches, wired/wireless output and permanent technology/magic core upgrades.
- Includes eight fuel upgrades, thirteen voltage stars, ten base fluids and ten exponent cores. Survival recipes for the controller, casing and route materials remain undecided.
- Targets GTNH 2.9.0 stable. Existing heat values, recipe amounts, structures, registry IDs and generation behavior are preserved.
- Reorganized bilingual READMEs, player guides, artwork records and developer evidence. Documentation uses mB / B without rescaling recipe data.

Validation: 18 unit tests and 821 dedicated-server checks passed; no game client was launched. [Evidence](docs/development/VALIDATION.md)

## 本地开发历史 / Local development history

- **0.4.2-alpha · 2026-11-02**：适配 GTNH 2.9.0 正式版；16 项单元测试、781 项后台检查。 / Stable-pack dependency update; 16 unit tests and 781 server checks.
- **0.4.1-alpha · 2026-10-29**：燃料名称与物品说明、线圈并行、永久核心升级和紧凑 GUI；目标 RC-2。 / RC-2 fuel text, coil parallelism, persistent cores and compact GUI.

此前版本未在此仓库发布。 / Earlier versions were local builds and have no releases in this repository.
