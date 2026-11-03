#!/usr/bin/env python3
"""Draw 64px controller overlays on the unchanged native casing, and seam proofs.

Pillow only; no Minecraft client. Run before select_structure_materials.py.
"""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
import hashlib
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
GT = ROOT.parents[1] / 'work/gtnh-machine-validation/server/mods/gregtech-5.09.54.205.jar'
ICON = 'assets/gregtech/textures/blocks/iconsets/'
DEST = ROOT/'src/main/resources/assets/benzenefuels/textures/blocks'
ART = ROOT/'preview/controller'
DEST.mkdir(parents=True, exist_ok=True)
ART.mkdir(parents=True, exist_ok=True)
z = ZipFile(GT)


def native(name):
    im = Image.open(BytesIO(z.read(ICON+name+'.png'))).convert('RGBA')
    return im.crop((0, 0, im.width, im.width))


base = native('MACHINE_CASING_CHEMICALLY_INERT').resize((64,64), Image.Resampling.NEAREST)
base.save(ART/'controller_side.png')
fronts = {}
for active in (False, True):
    state = 'active' if active else 'idle'
    overlay = Image.new('RGBA', (64,64))
    d = ImageDraw.Draw(overlay)
    dark=(18,31,39,255); metal=(130,158,174,255); white=(216,236,244,255)
    blue=(88,210,238,255) if active else (53,105,129,255)
    light=(221,253,255,255) if active else (110,158,179,255)
    # Every outer 8px remains transparent: preserve the native bolts and seams.
    d.rounded_rectangle((10,9,53,54),radius=3,fill=(38,53,63,255),outline=metal,width=1)
    d.line((12,10,51,10),fill=white,width=1)
    d.rectangle((13,14,18,41),fill=dark,outline=(85,110,123,255))
    # Diesel-engine-inspired paired status lamps, recolored into the tower palette.
    d.rectangle((14,16,17,24),fill=blue if active else (31,58,73,255))
    d.rectangle((14,29,17,38),fill=(38,76,94,255) if active else blue)
    # Turbine-inspired hub, with two open resonance arcs rather than rotor blades.
    d.ellipse((21,14,50,43),fill=dark,outline=(77,105,120,255),width=2)
    d.arc((24,17,47,40),18,160,fill=blue,width=3)
    d.arc((24,17,47,40),198,340,fill=blue,width=3)
    d.arc((28,21,43,36),108,250,fill=light,width=2)
    d.arc((28,21,43,36),288,70,fill=light,width=2)
    d.rectangle((33,26,38,31),fill=light)
    for x,y in ((35,12),(35,44),(19,28),(51,28)):
        d.line((x-1,y,x+1,y),fill=white,width=1)
    d.rectangle((14,46,49,50),fill=dark)
    for x in range(16,47,5):
        d.rectangle((x,47,x+2,49),fill=blue if active or x<28 else (48,65,75,255))
    d.line((23,52,47,52),fill=(98,124,139,255))
    overlay.save(DEST/f'controller_front_{state}.png')
    glow=Image.new('RGBA',(64,64)); pixels=glow.load()
    for y in range(64):
        for x in range(64):
            rgba=overlay.getpixel((x,y))
            if active and rgba in (blue,light):pixels[x,y]=rgba
    glow.save(DEST/f'controller_front_{state}_glow.png')
    front=Image.alpha_composite(base,overlay)
    fronts[state]=front
    front.save(ART/f'controller_front_{state}.png')
    # Confirm the composited border is pixel-identical to native casing.
    for y in range(64):
        for x in range(64):
            if x<8 or x>=56 or y<8 or y>=56:
                assert front.getpixel((x,y))==base.getpixel((x,y))
    mosaic=Image.new('RGBA',(192,192))
    for x in range(3):
        for y in range(3):mosaic.paste(front if (x,y)==(1,1) else base,(x*64,y*64))
    mosaic.save(ART/f'controller_surround_{state}.png')

fontpath='/System/Library/Fonts/Supplemental/Arial.ttf'
font=ImageFont.truetype(fontpath,22)
small=ImageFont.truetype(fontpath,16)
sheet=Image.new('RGB',(1040,610),(229,236,241));d=ImageDraw.Draw(sheet)
d.text((28,20),'RESONANCE TOWER / CONTROLLER',font=font,fill=(25,47,62))
for i,state in enumerate(('idle','active')):
    x=28+i*350
    mosaic=Image.open(ART/f'controller_surround_{state}.png')
    sheet.paste(mosaic.resize((336,336),Image.Resampling.NEAREST),(x,62))
    d.text((x,410),state.upper()+' / native casing surround',font=small,fill=(38,61,76))
# An enlarged 1:1 front is useful for reviewing the original pixel artwork.
sheet.paste(fronts['active'].resize((280,280),Image.Resampling.NEAREST),(740,90))
d.text((740,410),'64 x 64 / front face',font=small,fill=(38,61,76))
d.text((28,455),'Unchanged casing at the edges; identical side, top and back casing.',font=small,fill=(38,61,76))
d.text((28,488),'Reference: GTNH diesel engine status lamps + large gas turbine hub.',font=small,fill=(38,61,76))
d.text((28,521),'Separate emissive overlay; controller is implemented in 0.3.0.',font=small,fill=(38,61,76))
sheet=sheet.crop((0,0,1040,565))
sheet.save(ART/'controller-design-sheet.png')
sources=['MACHINE_CASING_CHEMICALLY_INERT','OVERLAY_FRONT_DIESEL_ENGINE',
         'OVERLAY_FRONT_DIESEL_ENGINE_ACTIVE','LARGETURBINE_NEW5','LARGETURBINE_NEW_ACTIVE5']
report={'target':'GTNH 2.9.0-RC-2 / GT5 5.09.54.205','resolution':[64,64],
        'base_casing':'gregtech:gt.blockcasings8:0','border_unchanged_px':8,
        'idle_and_active_border_matches_native':True,'controller_registered':True,
        'source_license':'LGPL-3.0','source_textures':[
            {'resource':ICON+n+'.png','sha256':hashlib.sha256(z.read(ICON+n+'.png')).hexdigest(),
             'use':'derived casing background' if i==0 else 'visual reference only'}
            for i,n in enumerate(sources)],
        'layers':'Native casing + transparent idle/active front overlay + separate glow overlay.',
        'rendering_note':'No animation or shader bloom. Native glow rendering is used; light casings switch their light value with machine state.'}
(ROOT/'verification/controller-texture.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({'textures':4,'resolution':64,'edge_match':True,'runtime_controller':True}))
