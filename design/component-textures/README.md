# 部件材质 / Component textures

Benzene Fuels 0.4.5 · GTNH 2.9.0 · 2026-11-04

本组延续项目已有的代码绘制单元外壳，使用同一套像素几何、金属明暗和有限色阶制作。参考 GTNH 物品在小尺寸下的轮廓与分层方式；没有复制上游贴图像素，也没有使用新的图像生成调用。可编辑源为 [绘图脚本](../../tools/build_component_textures.py)。

- 苯环活化剂：浅银蓝晶粒与陶瓷／金属托盘。
- 苯环量子催化剂：六角金属载体、苯环中心、金色固定点。
- LV～UXV 之星：四向晶面，等级配色与递增金属框、触点。
- 科技指数核心 Mk1～Mk5：蓝白电路与金色引脚，1～5 个等级触点。
- 魔法指数核心 Mk1～Mk5：紫色切面晶体、银色符文框，1～5 个等级触点。
- 十种底数流体及单元：科技蓝白、魔法紫色；颜色随等级变亮，单元外壳与燃料单元一致。
- UV 苯活化液：淡紫活化流。

**35 种新物品图标 + 11 种流体贴图**。固体采用 32×32 的像素几何，整数放大至 64×64；没有模糊插值。流体与底数单元采用 64×4096 竖向动画条，64 帧、每帧 2 tick，与现有燃料相同。单元外壳和等级标记静止。八级燃料及其单元的既有动画不变。

- [总览](../../preview/components/all-components.png)
- [动画总览](../../preview/components/all-components.gif)
- [交互预览](../../preview/components.html)
- [文件与哈希清单](../../preview/components/manifest.json)

再生成：安装作者环境依赖 Pillow、NumPy 后运行 `python3 tools/build_component_textures.py`。玩家无需这些依赖。资源通过 Minecraft 原生图集与 mcmeta 加载。

## English

This original pixel-art set extends the existing code-authored cell shell. It uses consistent metal shading, limited palettes and readable silhouettes inspired by native GTNH item presentation, without copying upstream sprite pixels or making new image-generation calls. The script is the editable source.

There are 35 new item sprites and 11 fluid sprites. Static items use 32-pixel geometry enlarged to 64 pixels with nearest-neighbor sampling. Fluids and base cells have 64 native animation frames at two ticks per frame; their shells and tier contacts remain fixed. Existing fuel art, recipes, IDs and gameplay are unchanged. Pillow/NumPy are authoring dependencies only.
