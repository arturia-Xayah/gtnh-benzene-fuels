# LuV / ZPM 苯的燃气涡轮对比

目标版本：GTNH 2.9.0-RC-2，GT5 5.09.54.205。共振燃料发电塔确定按 UV 解锁，生存配方待设计。

本表读取隔离后台服务器实际加载的材料参数，按该版本大型/特大燃气轮机源码计算；未启动图形客户端，未进行涡轮世界内运行测试。

燃料热值以原生流体计数 mB 为单位：LuV 13,940 EU/mB，ZPM 136,200 EU/mB。新塔 GUI 的整升（1000mB）不用于本表。假设维护满效率、稳态、供油精确到计算所得最佳流量、动力仓足够；不计管线损耗和转子成本。不含超载。

大型燃气轮机主机使用 EV 外壳和电路。特大燃气轮机使用 LuV 组装配方、Zeron-100 部件和 LuV 电路。

LuV 表使用大型尺寸转子（工具 ID 174）；ZPM 表使用巨型尺寸转子（工具 ID 176，需要长镅杆）。尺寸不等于机器规格。

贫铀若指 Staballoy（贫铀合金）：运行时 GT 材料表没有此材料，GT++ 注册的是配方部件 rotor，不能当作这些轮机使用的 GT 涡轮转子。极不稳定硅岩为 GoodGenerator 的 Extremely Unstable Naquadah，非 Naquadria。

|燃料|转子|机器|模式|消耗 mB/t|输出 EU/t（约）|有效效率|
|---|---|---|---|---:|---:|---:|
|LuV|精金|LGT|紧装，最佳流量|1|28,577|205.00%|
|LuV|精金|XLGT|紧装，最佳流量|27|771,579|205.00%|
|LuV|铿铀|LGT|紧装，最佳流量|5|149,855|215.00%|
|LuV|铿铀|XLGT|紧装，最佳流量|88|2,637,448|215.00%|
|LuV|灵宝|LGT|紧装，最佳流量|2|62,730|225.00%|
|LuV|灵宝|XLGT|紧装，最佳流量|33|1,035,045|225.00%|
|LuV|极不稳定硅岩|LGT|紧装，最佳流量|1|29,971|215.00%|
|LuV|极不稳定硅岩|XLGT|紧装，最佳流量|17|509,507|215.00%|
|ZPM|精金|LGT|高热值强制限功率|1|73,600|54.04%|
|ZPM|精金|XLGT|高热值强制限功率|1|73,600|54.04%|
|ZPM|铿铀|LGT|松装，最佳流量|7|1,666,543|174.80%|
|ZPM|铿铀|XLGT|紧装，最佳流量|12|3,922,560|240.00%|
|ZPM|灵宝|LGT|高热值强制限功率|1|96,000|70.48%|
|ZPM|灵宝|XLGT|高热值强制限功率|1|96,000|70.48%|
|ZPM|极不稳定硅岩|LGT|高热值强制限功率|1|48,000|35.24%|
|ZPM|极不稳定硅岩|XLGT|高热值强制限功率|1|48,000|35.24%|

当前源码中，大型与特大轮机都先比较单个转子的紧装最优功率与燃料热值。若燃料热值更高，直接消耗 1mB，输出单转子的紧装最优功率；特大机也不会在此分支应用 16 倍吞吐。此时改松装也不会绕过该分支。

巨型铿铀转子的紧装最优热输入为 102400 EU/t，效率为 240%，最优输出为约245760 EU/t。ZPM 苯热值落在这两个值之间：单台大型轮机通过前置比较，但后续最佳流量 int(102400/136200)=0，不能发电。松装为7mB/t、174.8%有效效率；特大机紧装为int(16*102400/136200)=12mB/t、240%有效效率。

特大机 12 转子 / 16 台吞吐是规模与转子成本优势；最佳流量时效率不额外乘16或4/3。整数流量取整会使单台最优输出低于面板连续流量理论值。

依据：
- work/material-tier-review/source/gregtech/api/util/TurbineStatCalculator.java
- work/material-tier-review/source/gregtech/common/tileentities/machines/multi/turbines/MTELargeTurbineGas.java
- work/material-tier-review/source/gregtech/common/tileentities/machines/multi/xlturbines/MTEXLTurbineGas.java
- work/material-tier-review/source/gtPlusPlus/core/recipe/RecipesMachinesCustom.java
- work/turbine-benzene-audit/server/turbine-audit.json
