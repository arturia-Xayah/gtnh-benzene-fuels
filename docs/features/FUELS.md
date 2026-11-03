# 燃料与升级配方

[中文首页](../../README.md) · [English README](../../README.en.md)

本文统一使用 mB / B：1 B = 1000 mB。配方和热值的原始数值保持不变；GT 部分界面会将原始流体单位标为 L。

| 阶段 | 名称 | EU/mB |
|---|---|---:|
| LuV | 超富集铂族苯 | 13,940 |
| ZPM | ZPM苯 | 136,200 |
| UV | 中子过饱和苯 | 1,491,000 |
| UHV | UHV苯 | 18,290,000 |
| UEV | UEV苯 | 251,500,000 |
| UIV | 因果闭环苯 | 3,876,000,000 |
| UMV | 永恒调律苯 | 66,950,000,000 |
| UXV | 苯征宇宙燃料 | 1,296,000,000,000 |

从硝基苯 1,600 EU/mB 出发，每级乘 `4.4^(1+目标电压等级/13)`，逐级截断至四位有效数字。完整热值使用 long 保存。LuV～UEV 注册原版燃气配方；UIV～UXV 超出原版 int 热值范围，由共振燃料发电塔读取完整热值；不强行截断或注册虚假燃值。

原版涡轮和输出仓的效率、流量、功率限制仍然适用。共振燃料发电塔已实现，支持完整long热值及BigInteger能量结算，规则见下文。

物品说明显示燃料等级、1B容量、K/M/G/T等缩写的基础热值（EU/mB），以及同体积原版燃料当量；保留四位有效数字。物品说明仅保留等级、容量、热值、燃料当量及配套文案；每级另有一句配套文案，采用原生TecTech机械方块的青色粗体并加斜体（§b§l§o）。UV固定参照激发铀基燃料，UHV固定参照硅岩基燃料MkI，均读取当前配置，允许当量小于1；其余等级取不高于本品热值的最高基准：硝基苯读取已加载配方，硅岩基燃料MkI～VI读取当前配置，反物质按等量铜、指数1的1T EU/mB计算，不包含发电设备增益。UXV燃料为1.296倍反物质基准。ZPM、UHV、UEV暂用等级占位名；流体ID及配方不变。

## 每批升级

每批 **50,000 mB 上级燃料 → 50,000 mB 本级燃料**，200 tick、1 A 对应阶段电压。以下为持续消耗量。

| 输出 | 机器 | 额外输入 |
|---|---|---|
| LuV | 大型化反 | 活化剂1；由铂、铑、钯、铱、钌粉各1,024＋氦等离子体16,384 mB，在LuV装配线10秒制成 |
| ZPM | 聚变Mk-II+ | 锡等离子体163,840 mB；启动280 MEU |
| UV | 聚变Mk-III+ | 活化液144 mB；由中子素粉、宇宙中子态素粉各1,024＋镅等离子体16,384 mB，在UV大型化反10秒制成；启动500 MEU |
| UHV | 硅岩燃料精炼厂，1级线圈+ | 无尽催化剂粉、原子分离催化剂粉各1,024；熔融无尽17,856 mB |
| UEV | 2级量子操纵者+ | 超时空金属粉、海珀珍粉各1,024；熔融无尽17,856 mB |
| UIV | 3级量子操纵者+ | 超时空金属粉、海珀珍粉各2,048；熔融时空17,856 mB |
| UMV | 4级量子操纵者 | 调律源金144 mB、永恒144 mB、白矮星物质16 mB |
| UXV | 4级量子操纵者，UXV供电 | 宇宙素124 mB、磁流体约束恒星物质124 mB、浓缩原始恒星等离子体混合物1,984 mB、永恒144 mB |

大宗催化剂仓中的苯环量子催化剂不消耗，每个提供1并行；其建设配方保持为铂、钯、铱粉各8＋UEV力场发生器1＋无尽144 mB。活化剂的中间产物数量没有重复放大。

## English

All upgrades consume 50,000 mB of the previous fuel and output 50,000 mB, taking 200 ticks at 1 A of their output tier.

| Tier | EU/mB |
|---|---:|
| LuV | 13,940 |
| ZPM | 136,200 |
| UV | 1,491,000 |
| UHV | 18,290,000 |
| UEV | 251,500,000 |
| UIV | 3,876,000,000 |
| UMV | 66,950,000,000 |
| UXV | 1,296,000,000,000 |

Values start at Nitrobenzene's 1,600 EU/mB and multiply by `4.4^(1+destination_tier/13)`, truncating each stage to four significant digits. Full values use long arithmetic. Only LuV through UEV fit native integer gas-fuel recipes; UIV through UXV retain their full heat values and crafting recipes, supported by the dedicated generator. No overflowing or capped native fuel entries are registered.

Recurring raw solids are multiplied by 1,024. Helium, tin and americium plasma costs are multiplied by 16,384. Molten Infinity, Spacetime, Universium, MHDCSM and Concentrated Raw Star Matter costs are multiplied by 124. Shirabon, Eternity and White Dwarf Matter remain unchanged. Intermediate activators are not multiplied again. Reusable quantum-catalyst construction is unchanged. See NEI or the Chinese table for exact per-batch inputs.

Native rotor, efficiency and dynamo limits still apply. The dedicated Resonance Fuel Generator is implemented; see its runtime rules below.

Fuel-cell tooltips show the voltage tier, 1B volume, compact K/M/G/T base heat in EU/mB and an equal-volume native-fuel equivalent, truncated to four significant digits. A localized flavor line uses the native TecTech casing style: aqua and bold, with added italics (§b§l§o). UV compares against configured excited uranium fuel; UHV compares against configured Naquadah fuel MkI, allowing ratios below 1. Other references use the loaded Nitrobenzene recipe, configured Naquadah fuels MkI–VI and the balanced-copper antimatter baseline (1T EU/mB, exponent 1), without generator boosts. UXV is 1.296 times that antimatter baseline. ZPM/UHV/UEV names remain tier placeholders; saved fluid IDs and recipes are unchanged.
