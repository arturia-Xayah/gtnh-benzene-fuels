#!/usr/bin/env python3
"""Pack authored fuel art into looping Minecraft fluid/cell sprites and previews.

Requires Pillow and NumPy for asset authoring only; the mod has no new dependency.
"""
from __future__ import annotations

import base64
import hashlib
import json
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "design/fuel-textures"
ASSETS = ROOT / "src/main/resources/assets/benzenefuels/textures"
PREVIEW = ROOT / "preview/fuel-textures"
SIZE, FRAMES, FRAME_TICKS = 64, 64, 2
STAGES = [
    ("luv", "LuV", "超富集铂族苯", "氦等离子体 · 铂 / 铑 / 钯 / 铱 / 钌", "银蓝金属流与铂族微粒", (1, 0)),
    ("zpm", "ZPM", "ZPM苯", "上级燃料 · 锡等离子体", "液态锡银纹与青色电离弧", (0, 1)),
    ("uv", "UV", "中子过饱和苯", "中子素 · 宇宙中子素 · 镅等离子体", "紫色高密流与中子闪点", (1, 1)),
    ("uhv", "UHV", "UHV苯", "无尽催化剂 · 原子分离催化剂 · 熔融无尽", "暗底虹彩金属波纹", (-1, 0)),
    ("uev", "UEV", "UEV苯", "超越金属 · 海珀珍 · 熔融无尽", "赤紫流与金色高能微粒", (0, -1)),
    ("uiv", "UIV", "因果闭环苯", "超越金属 · 海珀珍 · 熔融时空", "暗紫剪切流与稀疏星点", (-1, 1)),
    ("umv", "UMV", "永恒调律苯", "灵宝 · 永恒 · 白矮星物质", "青蓝冷流、白矮星亮点与红色弧线", (1, -1)),
    ("uxv", "UXV", "苯征宇宙燃料", "宇宙素 · 磁流体约束恒星物质 · 浓缩原始恒星物质 · 永恒", "银白宇宙素与受约束的金色恒星流", (-1, -1)),
]


def seamless_seed(path: Path) -> np.ndarray:
    """Keep the authored image and gently reconcile opposite edge pixels."""
    image = Image.open(path).convert("RGB").resize((SIZE, SIZE), Image.Resampling.BOX)
    a = np.asarray(image, dtype=np.float64).copy()
    for axis in (0, 1):
        edge = (np.take(a, 0, axis=axis) + np.take(a, SIZE - 1, axis=axis)) / 2
        for depth, weight in enumerate((1, .60, .25)):
            for pos in (depth, SIZE - 1 - depth):
                idx = [slice(None)] * 3
                idx[axis] = pos
                a[tuple(idx)] = a[tuple(idx)] * (1 - weight) + edge * weight
    return a


def sample_periodic(a: np.ndarray, x: np.ndarray, y: np.ndarray) -> np.ndarray:
    x, y = x % SIZE, y % SIZE
    x0, y0 = np.floor(x), np.floor(y)
    ix, iy = x0.astype(int) % SIZE, y0.astype(int) % SIZE
    fx, fy = (x - x0)[..., None], (y - y0)[..., None]
    return (a[iy, ix] * (1 - fx) * (1 - fy)
            + a[iy, (ix + 1) % SIZE] * fx * (1 - fy)
            + a[(iy + 1) % SIZE, ix] * (1 - fx) * fy
            + a[(iy + 1) % SIZE, (ix + 1) % SIZE] * fx * fy)


def fluid_frames(seed: np.ndarray, direction: tuple[int, int]) -> list[Image.Image]:
    y, x = np.mgrid[:SIZE, :SIZE]
    out = []
    tau = 2 * np.pi
    # Integer tile drift plus periodic shear returns to the same field after one loop.
    for frame in range(FRAMES):
        t = frame / FRAMES
        sx = x + direction[0] * SIZE * t + 2.2 * np.sin(tau * y / SIZE + tau * t)
        sy = y + direction[1] * SIZE * t + 1.6 * np.cos(tau * x / SIZE - tau * t)
        a = sample_periodic(seed, sx, sy)
        # Only bright particles breathe; no global flashing or hue cycling.
        light = np.clip((a.max(axis=2) - 165) / 90, 0, 1)
        a *= 1 + .045 * np.sin(tau * t * 2 + tau * (x + y) / SIZE)[..., None] * light[..., None]
        a = np.clip(np.rint(a), 0, 255).astype("uint8")
        out.append(Image.fromarray(a).convert("RGBA"))
    return out


def cell_frame(liquid: Image.Image) -> Image.Image:
    """Original pixel-drawn metal cell; frame/glass stay fixed, liquid animates."""
    scale = 2
    shell = Image.new("RGBA", (32, 32))
    d = ImageDraw.Draw(shell)
    body = [(8, 5), (23, 5), (26, 8), (26, 26), (23, 29), (8, 29), (5, 26), (5, 8)]
    d.polygon(body, fill="#111d30")
    d.polygon([(9, 6), (22, 6), (25, 9), (25, 25), (22, 28), (9, 28), (6, 25), (6, 9)], fill="#7994ad")
    d.polygon([(9, 7), (22, 7), (24, 9), (24, 25), (22, 27), (9, 27), (7, 25), (7, 9)], fill="#25374b")
    d.line([(7, 24), (7, 9), (9, 7), (22, 7)], fill="#dceef6")
    d.line([(24, 10), (24, 25), (22, 27), (10, 27)], fill="#39516c")
    mask = Image.new("L", (32, 32))
    window = [(10, 9), (21, 9), (23, 11), (23, 23), (21, 25), (10, 25), (8, 23), (8, 11)]
    ImageDraw.Draw(mask).polygon(window, fill=255)
    shell = shell.resize((SIZE, SIZE), Image.Resampling.NEAREST)
    shell.paste(liquid, (0, 0), mask.resize((SIZE, SIZE), Image.Resampling.NEAREST))
    glass = Image.new("RGBA", (32, 32))
    g = ImageDraw.Draw(glass)
    g.line([(10, 10), (10, 16)], fill=(230, 249, 255, 115), width=1)
    g.point((11, 10), fill=(255, 255, 255, 145))
    g.line([(22, 20), (22, 23), (21, 24)], fill=(255, 255, 255, 50))
    # Cap, collar and two mounting bolts do not move or get fuel-tinted.
    g.rectangle((11, 2, 20, 5), fill="#142236")
    g.rectangle((12, 2, 19, 3), fill="#d6e8f2")
    g.line((12, 4, 19, 4), fill="#506c86")
    g.rectangle((10, 6, 21, 7), fill="#b1c8d9")
    g.line((12, 6, 19, 6), fill="#e7f6ff")
    g.rectangle((11, 26, 20, 27), fill="#8fa9bd")
    g.point((8, 8), fill="#eef8ff")
    g.point((23, 26), fill="#c6e0f0")
    g.line((13, 27, 18, 27), fill="#5abddd")
    return Image.alpha_composite(shell, glass.resize((SIZE, SIZE), Image.Resampling.NEAREST))


def write_strip(frames: list[Image.Image], path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    strip = Image.new("RGBA", (SIZE, SIZE * FRAMES))
    for i, frame in enumerate(frames):
        strip.paste(frame, (0, SIZE * i))
    strip.save(path, optimize=True)
    path.with_suffix(".png.mcmeta").write_text(json.dumps({"animation": {"frametime": FRAME_TICKS}}, indent=2) + "\n")


@lru_cache(maxsize=8)
def font(size: int) -> ImageFont.FreeTypeFont:
    candidates = ["/System/Library/Fonts/Hiragino Sans GB.ttc",
                  "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"]
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default(size=size)


def sheet(frames_by_stage: dict, frame: int) -> Image.Image:
    canvas = Image.new("RGB", (1200, 684), "#09121f")
    d = ImageDraw.Draw(canvas)
    d.text((30, 18), "八级苯燃料 / ANIMATED FUEL COLLECTION", font=font(23), fill="#e9f3ff")
    d.text((31, 56), "铂族金属 → 等离子体 → 无尽 → 时空 → 恒星物质", font=font(15), fill="#96b0ca")
    for i, (key, tier, name, ingredients, motif, _) in enumerate(STAGES):
        x, y = 24 + (i % 4) * 294, 96 + (i // 4) * 284
        d.rounded_rectangle((x, y, x + 278, y + 268), radius=12, fill="#101e30", outline="#24384f")
        d.text((x + 14, y + 12), tier, font=font(18), fill="#9ad9ff")
        d.text((x + 14, y + 42), name, font=font(16), fill="#edf4ff")
        fluid, cell = frames_by_stage[key]
        canvas.paste(fluid[frame].resize((160, 160), Image.Resampling.NEAREST), (x + 14, y + 81))
        item = cell[frame].resize((88, 88), Image.Resampling.NEAREST)
        canvas.paste(item, (x + 181, y + 117), item)
    return canvas


def main() -> None:
    PREVIEW.mkdir(parents=True, exist_ok=True)
    rows, sprites = [], {}
    for key, tier, name, ingredients, motif, direction in STAGES:
        frames = fluid_frames(seamless_seed(ART / "source" / (key + ".png")), direction)
        cells = [cell_frame(f) for f in frames]
        sprites[key] = (frames, cells)
        row = {"key": key, "tier": tier, "name": name, "ingredients": ingredients, "motif": motif,
               "size": SIZE, "frames": FRAMES, "frameTicks": FRAME_TICKS, "loopSeconds": FRAMES * FRAME_TICKS / 20}
        for kind, images, folder in [("fluid", frames, "blocks/fluids"), ("cell", cells, "items")]:
            filename = f"benzene_{key}.png" if kind == "fluid" else f"cell_{key}.png"
            path = ASSETS / folder / filename
            write_strip(images, path)
            preview = PREVIEW / f"{key}-{kind}.png"
            preview.write_bytes(path.read_bytes())
            row[kind + "Sprite"] = "fuel-textures/" + preview.name
            row[kind + "Data"] = "data:image/png;base64," + base64.b64encode(path.read_bytes()).decode()
            row[kind + "Sha256"] = hashlib.sha256(path.read_bytes()).hexdigest()
            gif = PREVIEW / f"{key}-{kind}.gif"
            images[0].save(gif, save_all=True, append_images=images[1:], duration=FRAME_TICKS * 50,
                           loop=0, disposal=2, optimize=False)
        rows.append(row)
    # A contact animation renders the very same frames packaged in the JAR.
    shots = [sheet(sprites, i) for i in range(FRAMES)]
    shots[0].save(PREVIEW / "all-fuels.png")
    shots[0].save(PREVIEW / "all-fuels.gif", save_all=True, append_images=shots[1:],
                  duration=FRAME_TICKS * 50, loop=0, optimize=True)
    (PREVIEW / "manifest.json").write_text(json.dumps([{k: v for k, v in r.items() if not k.endswith("Data")} for r in rows], ensure_ascii=False, indent=2) + "\n")
    template = (ROOT / "design/fuel-textures/preview-template.html").read_text()
    (ROOT / "preview/fuel-textures.html").write_text(template.replace("__FUEL_DATA__", json.dumps(rows, ensure_ascii=False)))
    print(f"Generated {len(rows)} fluids + {len(rows)} animated cells; {SIZE}px, {FRAMES} frames, {FRAME_TICKS} ticks/frame")


if __name__ == "__main__":
    main()
