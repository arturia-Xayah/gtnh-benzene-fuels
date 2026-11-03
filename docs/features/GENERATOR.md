# 共振燃料发电塔

[中文首页](../../README.md) · [English README](../../README.en.md)

主机在创造模式GT机器栏／NEI中取得，生存合成配方待定。已有底数材料和导光外壳的生存配方同样待定。

- 每批100tick（5秒）；主机右下角设置按钮打开侧边窗口，设置**设定并行上限**，范围1～2,147,483,647，默认1；本机配置的1升固定等于1,000mB。
- 中央5×5截面外圈包覆1,648个加热线圈（Y=8～110，每层16个），全部须同级；缺失或混级无法成型。`线圈并行上限 = floor(4^((热量K - 901) / 900.0 - 1))`，使用实际热量和实数除法。白铜1801K为1并行、坎塔尔2701K为4并行。默认设定1并行，每并行每批消耗1000mB（1B）燃料；原来的超导线圈装饰不计入此上限。
- 每批消耗 `min(可用燃料mB整除1000, 设定并行上限, 线圈并行上限) × 1000 mB`；不足1,000mB不启动，多余不足一升的尾量保留。
- 每1,000mB燃料各消耗1mB科技、魔法底数流体。三者必须一次性满足整批数量；底数不足时不缩小批次，不扣任何原料，无小数流体或底数余额。
- 按结构扫描顺序、再按仓内槽顺序读取。跳过底数流体后，仅检查第一种流体；不受支持或不足一升时不尝试后续燃料。同种燃料可跨仓汇总。两侧底数各使用首先读到的种类，不混合不同Mk。
- 支持本模组八级燃料与原生燃气燃料表中的流体。**热值沿用现有GT燃料注册值，按每个原始流体单位mB结算**，不因GUI使用升而另缩放热值。GT原有表格/界面使用的L标记和本机1升=1000mB的配置口径需区别。
- 小游戏系数及外层指数目前为1；倍率 `科技底数^科技指数 × 魔法底数^魔法指数`。两侧未升级指数各为1；科技核心指数依次为2、4、8、16、32，魔法核心为2、4、6、8、10。
- 例：1,000mB 超富集铂族苯＋两侧Mk1底数各1mB，产生14,220,194EU，在100tick分配；各tick相差最多1EU，完整输出总和准确。
- 原生GT连续配方生命周期：自动接下一批。GUI显示线圈热量、并行上限、当前/设定并行、当前输入B及B/s、燃料效率、当前发电、实际发送及发送率、两侧底数流体名称和指数核心等级。功率使用K/M/G并截断至两位小数，超过1000G采用科学计数法。隐藏累计输出/损耗和四个无关配方按钮；无线模式使用独立按钮。每批启动时固定燃料和倍率，运行中修改设置仅影响下批。
- 有线模式按各动力仓的V×A和剩余缓存接收，多出的能量直接丢弃，不爆炸、不停机；不把剩余能量积攒到下一批。允许混用动力仓等级，包括多安动力仓。无线模式直接写入主机所有者的GT无线网络，可不安装动力仓。
- 进度、整批能量、剩余能量、上限、输出模式和累计输出/损耗写入NBT；卸载期间不额外发电。结构中断丢弃本批剩余能量，不退燃料；合法强制结构复查保留当前进度。

旧结构升级时，须按新版预览补齐中央线圈护套；存档中的原有上限设置保留，实际批量始终受当前线圈限制。

结构为**33×33×129，默认15,472个方块**；主机安装世界Y须为2～129。模型坐标以地基Y=0、前侧为-Z：主机(0,2,-16)。

| 区域 | 允许仓室 | 上限 |
|---|---|---:|
| 前侧Z=-16、Y=2或3的惰性机械方块 | 流体输入仓、维护仓 | 1～3输入＋恰好1维护 |
| 前侧最底排Y=1、地基层、显示面板及其余方块 | 不允许仓室替换 | 0 |
| 后侧Z=14、Y=2或3两排坚固钢机械方块 | 动力仓 | 0～16 |

支持水平四向旋转；暂不支持翻转或倾倒。后侧材料是坚固钢机械方块。默认预览3输入、1维护、1动力仓；新增动力仓替换对应钢外壳。完整清单：[材料用量](../../design/generator-materials.md) / [CSV](../../design/generator-bom.csv)。

主机使用64×64停机/运行覆盖层及独立发光层；621个蓝白导光外壳使用metadata 0/1切换停机/运行贴图，世界亮度0/15，掉落时恢复metadata 0。界面使用原生GT终端。网页可切换两种材质状态和前后仓室区域；特殊玻璃和游戏内渲染以客户端为准。

预览源 `preview/generator-structure.html`；同源模型 `design/generator-structure.json`；运行时模型打包在 `assets/benzenefuels/generator-structure-runtime.json`。构建提示及生存自动搭建接入StructureLib，仓室需按允许区域安装。配置文件 `config/benzenefuels.cfg` 的 `machines.generatorMetaId` 默认32240；客户端与服务端须使用相同ID，放置主机后不要更改此ID。

## English

The controller is available from the GT creative machine tab/NEI. Survival recipes for the controller, route materials and light casing remain undecided.

- A batch lasts 100 ticks (5 seconds). The settings button opens a side panel for configured parallelism:1–2,147,483,647, default 1. This controller defines 1 litre as 1,000 mB.
- The central 5×5 jacket has 1,648 same-tier heating coils, 16 per layer at Y=8–110. Missing or mixed-tier coils invalidate the structure. The parallel ceiling is `floor(4^((heat_K - 901) / 900.0 - 1))`, using real division and actual GT heat: 1801 K gives 1, 2701 K gives 4. One parallel burns 1000 mB per batch; the default setting is 1. Old structures need the new jacket; existing saved settings remain but are capped by installed coils. Decorative superconductor blocks do not count.
- Fuel consumed: `min(available_mB / 1000 rounded down, configured_parallel, coil_limit) * 1000`. Less than 1,000 mB does not start.
- Each 1,000 mB fuel needs 1 mB of each route base. All three inputs must cover the whole batch; insufficient bases cause no deductions or batch downscaling. No fractional fluid balances exist.
- Structure traversal order, then hatch slot order, is used. Only the first non-booster fluid is checked; invalid or insufficient first fuel blocks later fuels. Identical fluids can aggregate across hatches. Each route uses its first selected Mk, without mixing levels.
- Supports the eight addon fuels and native gas-fuel fluids. Heat values retain their native EU-per-fluid-unit (mB) meaning; the whole-litre GUI does not rescale fuel heat. Existing GT displays may label that native unit L.
- The minigame factor and outer exponent remain 1. Multiplier is `technology_base^technology_exponent * magic_base^magic_exponent`. Each unupgraded route has exponent 1. Technology cores grant 2/4/8/16/32; magic cores grant 2/4/6/8/10.
- Example:1,000 mB LuV fuel plus 1 mB of each Mk 1 base yields 14,220,194 EU over 100 ticks. Adjacent ticks differ by at most 1 EU and the exact batch total is conserved.
- Uses GT's continuous recipe lifecycle. The compact GUI shows batch B and B/s, fuel efficiency, generation, actual delivery with delivery percentage, base-fluid names and exponent tiers. Power uses two truncated decimals and K/M/G suffixes, switching to scientific notation above 1000G. Lifetime totals and the four unrelated recipe buttons are hidden. Wireless has a dedicated button. Settings and output mode changes apply next batch.
- Dynamos accept up to their native voltage×amperage and free buffer space. Excess energy is lost immediately, with no explosion or production blockage. Mixed tiers and multi-amp dynamos are supported. Wireless mode credits the owner's native GT wireless network and needs no dynamo.
- NBT preserves settings, progress and remaining batch energy. Unloaded machines do not produce. Structure interruption discards prepaid remaining energy; valid forced rechecks preserve progress.

The 33×33×129 tower has 15,472 blocks in the default 3-input/1-maintenance/1-dynamo layout. Controller world Y must be 2–129. Model origin is the foundation centre, front = -Z, controller = (0,2,-16). Front inert casings at Z=-16, Y=2 or 3 accept 1–3 fluid inputs and exactly 1 maintenance hatch. Bottom rowY=1 and all other front materials reject hatches. Dynamos (0–16) replace only the two rear steel rows at Z=14,Y=2 or 3. Horizontal rotations are supported; flips/tilts are disabled.

The controller has 64×64 idle/active front overlays and a native emissive layer. All 621 light casings switch metadata 0/1, idle/active textures and light values 0/15; drops reset to 0. StructureLib hints and survival construction are supported. See the [BOM](../../design/generator-materials.md), [CSV](../../design/generator-bom.csv) and [texture provenance](../../preview/TEXTURE-SOURCES.md). Preview source:`preview/generator-structure.html`; shared voxel source:`design/generator-structure.json`; runtime model:`assets/benzenefuels/generator-structure-runtime.json`.

`config/benzenefuels.cfg` reserves configurable GT meta-tile ID 32240. Client and server must use the same ID. Do not change it after placing controllers.
