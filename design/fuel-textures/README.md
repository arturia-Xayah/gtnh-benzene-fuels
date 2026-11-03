# 八级苯燃料 · 动画美术

2026-11-03 · 用于 Benzene Fuels 0.4.3-alpha / GTNH 2.9.0。

八种原画分别通过内置 **image_gen** 生成，源文件保存在 [source](source)；[完整提示词](art-direction.json)一并保留。以 GTNH 熔融时空的暗色星点和原生动画方式为参考，其他原料采用视觉化演绎，没有从上游纹理复制像素到这组燃料贴图。

| 等级 | 融合的本级原料 | 视觉特征 |
|---|---|---|
| LuV | 氦等离子体、铂、铑、钯、铱、钌 | 青蓝流、银白金属细纹、少量淡金微粒 |
| ZPM | 锡等离子体、上级苯燃料 | 银灰液态锡纹、青色电离弧 |
| UV | 中子素、宇宙中子素、镅等离子体 | 高密紫流、白色中子闪点、稀疏黄绿微粒 |
| UHV | 无尽催化剂、原子分离催化剂、熔融无尽 | 暗色底、粉青紫虹彩与银色微粒 |
| UEV | 超越金属、海珀珍、熔融无尽 | 赤紫能量流、金色亮点、少量冷色残光 |
| UIV | 超越金属、海珀珍、熔融时空 | 深紫空间剪切流、稀疏金白星点 |
| UMV | 灵宝、永恒、白矮星物质 | 青蓝冷流、红粉弧线与紧凑白色亮点 |
| UXV | 宇宙素、磁流体约束恒星物质、浓缩原始恒星物质、永恒 | 银白底、受约束的金色恒星流、冷色虹彩 |

所有配方仍使用原来的原料和用量；本表描述的是画面表达，没有改名或新增材料。

## 游戏资源

- 流体：`src/main/resources/assets/benzenefuels/textures/blocks/fluids/benzene_<tier>.png`。
- 单元：`src/main/resources/assets/benzenefuels/textures/items/cell_<tier>.png`。
- 每张 PNG 都是 **64×4096** 的竖向帧条，共 **64 帧、每帧 64×64**；同名 `.png.mcmeta` 指定每帧 2 tick，循环 6.4 秒。
- 流体仍图与流动图都指向该级专属贴图。其注册染色为白色，避免与自带配色相乘。
- 单元用原创像素金属外壳与玻璃高光，外框和透明轮廓固定，只有液体内容随对应流体动画变化。
- 星星、底数流体、指数核心及其他中间物不在本次八级燃料美术范围内，保留原来的临时资源。

没有新增客户端渲染器、着色器或运行依赖；动画交给 Minecraft 原生纹理图集。安装新 Mod 时客户端、服务端仍需使用相同版本。这里只更新资源和图标接入，燃料 ID、热值、配方、消耗和发电参数不变。

## 预览与再生成

[交互预览](../../preview/fuel-textures.html)是自包含 HTML，可直接打开。支持暂停、逐帧、速度和 2×2 平铺；动画帧与打包资源逐字节对应。[GIF 总览](../../preview/fuel-textures/all-fuels.gif)展示八级及对应单元。

`tools/build_fuel_textures.py` 从保存的原画生成周期流动、液体/单元帧条、mcmeta、GIF 和 HTML。作者工具需要 Pillow / NumPy，使用本地现有的工作区运行环境；玩家安装包不需要 Python 或这些库。原画不会随 JAR 打包，游戏中只加载加工后的帧条。

## English

Eight original base textures were generated with the built-in image_gen tool; their recipe-inspired motifs and exact prompts are retained here. Each fluid and cell uses a 64×4096 native PNG strip: 64 square frames at two ticks each, a 6.4-second loop. The metal cell shell stays fixed while its interior animates. Assets are full color, so fuel tint is white. No shader, custom renderer or animation dependency is added. Fuel IDs, recipes and generation values are unchanged. The interactive HTML and GIF preview use the packaged frames directly.
