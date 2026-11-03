# 电压之星与科技／魔法路线

[中文首页](../../README.md) · [English README](../../README.en.md)

八类部件：马达、活塞、泵、输送带、机械臂、发射器、传感器、力场发生器。

| 星星 | 上级星数量 | 本级每种部件数量 | 原始时间 |
|---|---:|---:|---:|
| LV | 0 | 1 | 8 s |
| MV | 16 | 2 | 16 s |
| HV | 16 | 4 | 32 s |
| EV | 16 | 8 | 64 s |
| IV | 16 | 16 | 128 s |
| LuV | 16 | 32 | 256 s |
| ZPM | 4 | 64 | 512 s |
| UV | 4 | 128 | 1,024 s |
| UHV | 4 | 256 | 2,048 s |
| UEV | 4 | 512 | 4,096 s |
| UIV | 4 | 1,024 | 8,192 s |
| UMV | 4 | 1,024 | 16,384 s |
| UXV | 4 | 1,024 | 32,768 s |

每次输出1个星，消耗当前等级1 A电力。LV～IV使用九槽组装机；LuV起使用装配线。装配线先扫描上级之星取得对应研究数据，不额外添加焊料或流体成本。UV及之后每种部件超过64个，应使用支持大数量供料的ME输入总线；活化剂装配线同样如此。星星目前是可合成的材料，尚未指定与指数核心之间的兑换关系。

## 科技与魔法增益材料

| Mk | 科技底数 | 科技指数 | 魔法底数 | 魔法指数 |
|---|---:|---:|---:|---:|
| 1 | 1.01 | 2 | 1.01 | 2 |
| 2 | 1.02 | 4 | 1.05 | 4 |
| 3 | 1.05 | 8 | 1.10 | 6 |
| 4 | 1.07 | 16 | 1.20 | 8 |
| 5 | 1.10 | 32 | 1.30 | 10 |

10种底数流体及其1,000 L单元、装罐/倒出配方已注册。10种指数核心可在发电塔右下角物品格中逐级安装，两侧各从Mk1升至Mk5，每级吸收1个。重复、降级、跳级或其他物品不会被消耗，可以取回。升级永久保存，拆下主机后仍保留；运行中安装从下一批生效。底数流体和核心的生存合成配方待定。

## English

Eight families: motors, pistons, pumps, conveyors, robot arms, emitters, sensors and field generators. Each recipe outputs one star.

| Tier | Previous stars | Each current-tier component | Seconds |
|---|---:|---:|---:|
| LV | 0 | 1 | 8 |
| MV | 16 | 2 | 16 |
| HV | 16 | 4 | 32 |
| EV | 16 | 8 | 64 |
| IV | 16 | 16 | 128 |
| LuV | 16 | 32 | 256 |
| ZPM | 4 | 64 | 512 |
| UV | 4 | 128 | 1,024 |
| UHV | 4 | 256 | 2,048 |
| UEV | 4 | 512 | 4,096 |
| UIV | 4 | 1,024 | 8,192 |
| UMV | 4 | 1,024 | 16,384 |
| UXV | 4 | 1,024 | 32,768 |

LV through IV use the native nine-slot assembler. LuV and later use the assembly line; scan the previous star to obtain the native research data. Recipes add no unrequested solder or fluid costs. Assembly-line inputs above 64 require compatible supply such as ME input buses. The same applies to the enlarged LuV activator recipe. A UXV star expands to 157,076,594,688 components across all eight families. Stars have no decided conversion to exponent cores yet.

### Route materials

| Mk | Technology base | Technology exponent | Magic base | Magic exponent |
|---|---:|---:|---:|---:|
| 1 | 1.01 | 2 | 1.01 | 2 |
| 2 | 1.02 | 4 | 1.05 | 4 |
| 3 | 1.05 | 8 | 1.10 | 6 |
| 4 | 1.07 | 16 | 1.20 | 8 |
| 5 | 1.10 | 32 | 1.30 | 10 |

Both routes have five base fluids and five exponent-core items. The fluids have 1,000 mB cells and native filling/emptying recipes. The controller consumes base fluids and accepts exponent cores in its bottom-right slot. Each route upgrades strictly from Mk 1 through Mk 5, consuming one core per tier. Duplicate, lower-tier, skipped-tier and unrelated items remain removable. Upgrades persist through saves and controller pickup; upgrades installed during a batch apply to the next batch. Survival crafting recipes remain undecided.
