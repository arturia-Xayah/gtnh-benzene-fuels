# 发电机完整用料 / Generator bill of materials

**33×33×129，共15,472个方块，24种用料条目。** 包含全部地基、内部核心和装饰，不含空气、外接管线及主机内部物品；这是搭建用量，不是合成这些方块所需的原矿展开。

20种结构方块变体＋3种原生仓室＋1个主机。相同注册名和元数据已合并：导光外壳616＋信标5＝621；栏杆60＋天线20＝80；三路LuV输入仓合计3个。

入口面板已全部补齐到Z=-16；维护仓移到前侧，动力仓移到后侧两排钢外壳。中央立柱Y=8～110外包5×5截面的加热线圈护套，共1,648个。替换8个框架和8个导流方块，净增1,632格；全层须使用同级线圈。清单按默认3输入、1维护、1动力仓计算；额外动力仓每增加1个，减少1个后侧坚固钢机械方块。

| 材料 | 数量 | 64个/组换算 | 注册名:元数据 |
|---|---:|---|---|
| 量子玻璃 | 3,628 | 56组＋44个 | `tectech:tile.quantumGlass:0` |
| 洁净不锈钢机械方块 | 1,940 | 30组＋20个 | `gregtech:gt.blockcasings4:1` |
| 加热线圈（默认白铜，可整层升级） | 1,648 | 25组＋48个 | `gregtech:gt.blockcasings5:0` |
| 聚变机械方块 | 1,423 | 22组＋15个 | `gregtech:gt.blockcasings4:6` |
| 坚固钢机械方块 | 1,215 | 18组＋63个 | `gregtech:gt.blockcasings2:0` |
| 防冻机械方块 | 894 | 13组＋62个 | `gregtech:gt.blockcasings2:1` |
| 工业高强度混凝土 | 648 | 10组＋8个 | `gregtech:gt.blockcasings9:3` |
| 蓝白共振导光外壳 | 621 | 9组＋45个 | `benzenefuels:resonance_light_casing:0` |
| 格栅机械方块 | 551 | 8组＋39个 | `gregtech:gt.blockcasings3:10` |
| 高级过滤机械方块 | 516 | 8组＋4个 | `gregtech:gt.blockcasings9:1` |
| 化学惰性机械方块 | 514 | 8组＋2个 | `gregtech:gt.blockcasings8:0` |
| 冷却管道机械方块 | 440 | 6组＋56个 | `gregtech:gt.blockcasings10:9` |
| 遏制场机械方块 | 369 | 5组＋49个 | `gregtech:gt.blockcasings2:8` |
| 魔法机械方块 | 284 | 4组＋28个 | `gregtech:gt.blockcasings8:8` |
| 超导线圈方块 | 276 | 4组＋20个 | `gregtech:gt.blockcasings:15` |
| 聚四氟乙烯管道方块 | 267 | 4组＋11个 | `gregtech:gt.blockcasings8:1` |
| LuV气密机械方块 | 116 | 1组＋52个 | `gregtech:gt.blockcasings6:6` |
| 铁栏杆 | 80 | 1组＋16个 | `minecraft:iron_bars:0` |
| 数据存储机械方块 | 28 | 28个 | `gregtech:gt.blockcasings2:7` |
| 青铜齿轮箱机械方块 | 8 | 8个 | `gregtech:gt.blockcasings2:2` |
| LuV输入仓 | 3 | 3个 | `gregtech:gt.blockmachines:56` |
| 共振燃料发电塔主机 | 1 | 1个 | `gregtech:gt.blockmachines:32240` |
| LuV动力仓 | 1 | 1个 | `gregtech:gt.blockmachines:36` |
| 维护仓 | 1 | 1个 | `gregtech:gt.blockmachines:90` |

默认输入／动力仓按LuV选型，可使用其他等级。主机已注册；最多3个输入仓、1个维护仓位于前侧惰性外壳Y=2或3，最底排Y=1禁止放仓室。后侧Z=14、Y=2或3的两排坚固钢外壳支持0～16个动力仓。64个/组是数量换算。

主机坐标(0,2,-16)，正面朝-Z；周围8格同层化学惰性机械方块。前景保留原生边框，侧／顶／背采用同种底壳；正面提供停机／运行／独立发光覆盖层。主机与621个导光外壳均按运行状态切换材质。

Machine controller textures use the native casing background and preserve its border pixels. The 1648-coil jacket replaces 16 existing blocks and adds 1632 placed blocks without changing tower dimensions. Counts merge identical registry names and metadata. No client was launched.
