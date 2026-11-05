# 苯向星辰 / Benzene Beyond the Horizon

GTNH 2.9.0 原生 BetterQuesting 章节。共 46 个新任务，复用 7 个原版任务；共显示 53 个节点。任务文案跟随游戏语言，支持简体中文与英语。

## 安装与已有存档

将同版 Benzene Fuels JAR 放入客户端和服务端 `mods`，重启后在原版生物燃料章节之后找到本章。单人游戏只需客户端安装。无需另外下载任务 JSON，也不需要执行清空任务或重置进度命令。

首次安装会向 `config/betterquesting/DefaultQuests` 添加自己的任务和章节文件，并在 `QuestLinesOrder.txt` 中插入章节位置；原排序文件备份为 `QuestLinesOrder.txt.before-benzenefuels`。不覆盖已有任务定义。旧存档载入自己的任务数据库后，仅补充缺少的新增任务，原有进度及领奖记录保留。原版默认任务书重载也能读取新增章节。

配置 `config/benzenefuels.cfg` 的 `quests.enableChapter=false` 可停止自动补充；不会删除已经安装的章节或进度。管理员修改过的已有任务定义和布局不在启动时被覆盖。未来修改任务条件时应通过原版编辑工具管理；本版本的自动行为是补充缺失内容。

集成针对 GTNH 2.9.0 所带 BetterQuesting **3.8.92-GTNH**。未安装 BetterQuesting 时，燃料模组正常加载但不添加章节。缺少指定原版前置任务时跳过添加并记录原因，避免留下无法完成的前置。

## 布局与前置链

| 区域 | 新任务数 | 推进与奖励 |
|---|---:|---|
| 入门与燃料 | 14 | 入门 1；活化剂／活化液／催化剂 3；八级燃料 8；燃气与自动化说明 2 |
| 发电塔工程 | 6 | UV 燃料之后开放，勾选说明；备料清单可选，无物资奖励 |
| 科技侧 | 6 | 路线介绍及 Mk1～Mk5，勾选说明，无物资奖励 |
| 魔法侧 | 6 | 路线介绍及 Mk1～Mk5，勾选说明，无物资奖励 |
| 电压之星 | 14 | 入门说明与 LV～UXV 成品检测；每级 1 枚对应阶段技术员硬币 |

燃料路线为：硝基苯＋装配线＋聚变 MkI → 铂族活化剂 → LuV → 聚变 MkII / ZPM → UV 活化液＋聚变 MkIII / UV → 硅岩燃料精炼厂 / UHV → QFT＋量子催化剂 / UEV → UIV → UMV → UXV。

原版前置引用编号为 3133、1412、1212、1197、1202、2959、3287，引用它们原本的完成状态，不创建副本、不额外发原版奖励。八级燃料任务各检测 **16,000mB**，支持原生 BQ 可识别的流体容器；自带燃料单元携带 16 个即可。不消耗燃料。LuV～UV 各奖 2 枚化学家 III 硬币，UHV～UMV 各 3 枚，UXV 8 枚；活化剂与 UV 活化液各 1 枚，量子催化剂 2 枚。所有奖励均不可重复领取。

科技／魔法底数流体、指数核心、主机和蓝白导光外壳尚无生存配方。这些节点不要求提交它们，也不会奖励它们。发电塔仅提供 UV 工程指南，不能借此获得主机；星星目前也不是指数核心的材料。燃料主线的 UIV～UXV 生产任务可完成，但对应共振发电塔的正常生存入口仍待配方设计。

## 文案与数据来源

参考 GTNH 2.9.0 原任务中的硝基苯、装配线、三级聚变、硅岩燃料精炼厂、Chemical Pseudo Alchemy 和 LESU 等任务，采用相同的任务类型、颜色格式、提示／警告框、非消耗检测和可选材料清单。新文案按本模组已注册配方编写，包含用途、操作步骤与自动化提醒。配方数量均明确写为 mB；热值沿用 GT 原生 EU/mB。

作者入口为 [build_chapter.py](../../questbook/build_chapter.py)，生成资源中的原生 NBT-JSON 与两种语言。UUID 按固定命名空间与任务名生成，调整文案和坐标不改变任务身份。生成器检查前置引用、循环、节点重叠、说明支线和奖励约束。

```sh
python3 questbook/build_chapter.py
python3 questbook/build_preview.py
```

[可点击预览](../../questbook/preview.html) 读取同一套任务数据与坐标，支持切换语言和查看节点详情，并非游戏截图。直接下载源码后打开此文件即可离线查看。

## English

Install the matching JAR on client and server and restart. The chapter appears after the native biofuel chapter. Single-player only needs the client installation. No separate JSON download or quest reset is required.

The addon supplies 46 quests and references seven original milestones. Eight fuel tasks detect 16,000 mB without consumption, using native BQ fluid-container detection. Small, one-time profession-coin rewards follow native conventions. UV generator engineering, technology and magic are checkbox references; the casing checklist is optional. Thirteen star milestones form a separate manufacturing branch and do not consume their stars.

Base fluids, exponent cores, the controller and its light casing still have no survival recipes. No task requires or rewards those unavailable items, and none gates fuel production. UIV–UXV fuel production remains available, while the generator's survival entry is still pending recipe design.

The installer adds only missing addon definitions to the native defaults and existing-world databases. It keeps original quests, completion and claim history, and administrator edits. Chapter ordering is backed up before insertion; native default-book reloads retain the chapter. `quests.enableChapter=false` stops further insertion without deleting previously installed content. Missing native prerequisite IDs cause a logged skip; the integration is optional without BetterQuesting.

Authoring uses Python 3 with no external packages. Regenerate the chapter and bilingual preview with the two commands above. Data uses stable UUIDs, native typed JSON and native translation keys. Dedicated-server validation covers actual task detection, rewards, old-world merging, disk persistence and default reloads; no Minecraft client was launched.
