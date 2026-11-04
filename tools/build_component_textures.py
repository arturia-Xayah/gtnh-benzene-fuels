#!/usr/bin/env python3
"""Original code-authored pixel icons, extending the existing cell-shell art system.
Pillow/NumPy are authoring tools only. No upstream sprite pixels are copied.
"""
from pathlib import Path
import base64
import hashlib
import json
import math

import numpy as np
from PIL import Image, ImageDraw
from build_fuel_textures import cell_frame, write_strip, font

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/benzenefuels/textures'
OUT = ROOT / 'preview/components'
DARK, STEEL, LIGHT, WHITE = '#172332', '#61788c', '#bdd0da', '#e9f5fa'
TIERS = ['lv','mv','hv','ev','iv','luv','zpm','uv','uhv','uev','uiv','umv','uxv']
COLORS = ['#4eac60','#db9745','#e8cf55','#9296a5','#638cd5','#dc9cdb','#63dedb',
          '#8ee6aa','#ec6a77','#c07de7','#7e9af7','#fb9e9a','#f5d2e7']
TE = ['#52a5c9','#51b6ce','#6dcddd','#90dfeb','#d0f4fa']
MA = ['#9163b9','#a36cce','#bc85de','#d7a5e8','#f1d1f7']


def rgb(c): return tuple(bytes.fromhex(c.lstrip('#')))
def shade(c, k): return tuple(min(255, round(x*k)) for x in rgb(c))
def blank(): return Image.new('RGBA',(32,32))
def up(im): return im.resize((64,64), Image.Resampling.NEAREST)
def hexagon(cx,cy,r):
    return [(round(cx+math.cos(math.pi/3*i)*r),round(cy+math.sin(math.pi/3*i)*r)) for i in range(6)]

def ring(d,cx,cy,r,c):
    p=hexagon(cx,cy,r)
    d.line(p+[p[0]],fill=DARK,width=3)
    d.line(p+[p[0]],fill=c,width=1)
    d.ellipse((cx-2,cy-2,cx+2,cy+2),outline=c)


def activator():
    im=blank(); d=ImageDraw.Draw(im)
    # Compact catalyst grains on a beveled ceramic sample dish.
    d.polygon([(4,19),(9,14),(24,14),(28,19),(26,26),(7,26)],fill=DARK)
    d.polygon([(5,19),(10,16),(23,16),(27,19),(25,24),(8,24)],fill=STEEL)
    d.line([(6,19),(9,24),(25,24)],fill=LIGHT)
    for x,y,s in [(11,16,4),(20,17,4),(15,10,5),(22,11,3)]:
        p=hexagon(x,y,s);d.polygon(p,fill=DARK)
        d.polygon(hexagon(x,y,s-1),fill='#759fbc')
        d.line([(x-s+1,y),(x-1,y-s+1),(x+s-1,y)],fill='#e6eff4')
        d.line([(x,y),(x+s-1,y),(x,y+s-1)],fill='#3d577e')
    ring(d,15,11,3,'#bbd9f6')
    return im


def catalyst():
    im=blank();d=ImageDraw.Draw(im)
    # Replace the temporary ender eye with an industrial hexagonal catalyst carrier.
    d.polygon(hexagon(16,16,14),fill=DARK)
    d.polygon(hexagon(16,16,12),fill=STEEL)
    d.polygon(hexagon(16,16,10),fill='#32465e')
    d.line([(4,16),(10,6),(22,6)],fill=LIGHT,width=1)
    d.line([(28,16),(22,26),(10,26)],fill='#3d5169',width=2)
    ring(d,16,16,6,'#c5a3e4')
    for x,y in hexagon(16,16,10):
        d.rectangle((x-1,y-1,x+1,y+1),fill=DARK);d.point((x,y),fill='#e7cd79')
    d.point((14,13),fill=WHITE)
    return im


def star(tier):
    im=blank();d=ImageDraw.Draw(im);c=COLORS[tier]
    # Four faceted points read at 16 px; frame complexity increases every four tiers.
    p=[(16,1),(20,10),(29,15),(21,20),(16,30),(11,21),(2,16),(11,11)]
    d.polygon(p,fill=DARK)
    d.polygon([(16,3),(19,12),(27,16),(19,19),(16,28),(12,19),(4,16),(12,12)],fill=shade(c,.62))
    d.polygon([(16,3),(16,16),(4,16),(12,12)],fill=shade(c,1.18))
    d.polygon([(16,16),(27,16),(19,19),(16,28)],fill=c)
    if tier>=4:
        d.line([(7,10),(7,7),(10,7)],fill=LIGHT)
        d.line([(22,7),(25,7),(25,10)],fill=LIGHT)
        d.line([(7,22),(7,25),(10,25)],fill=STEEL)
        d.line([(22,25),(25,25),(25,22)],fill=STEEL)
    if tier>=8:
        d.line([(10,7),(13,5),(19,5),(22,7)],fill=c)
        d.line([(10,25),(13,27),(19,27),(22,25)],fill=c)
    d.polygon(hexagon(16,16,7),fill=DARK)
    d.polygon(hexagon(16,16,6),fill=STEEL)
    d.polygon(hexagon(16,16,4),fill=shade(c,.45))
    ring(d,16,16,3,c)
    d.point((14,13),fill=WHITE)
    # Small rank contacts are geometry, not tiny unreadable lettering.
    for i in range(tier%4+1): d.point((13+i*2,22),fill=WHITE)
    return im


def core(route,mk):
    im=blank();d=ImageDraw.Draw(im);c=(TE if route=='technology' else MA)[mk-1]
    if route=='technology':
        for i in range(7,26,4):
            d.rectangle((i,3,i+1,28),fill='#c6b68a');d.rectangle((3,i,28,i+1),fill='#c6b68a')
        d.rectangle((5,5,26,26),fill=DARK);d.rectangle((6,6,25,25),fill=STEEL)
        d.rectangle((8,8,23,23),fill='#243c51')
        d.line([(6,24),(6,6),(24,6)],fill=LIGHT)
        for y in [10,14,18]:
            d.line([(9,y),(11,y),(13,y+2)],fill=shade(c,.6))
            d.line([(23,y),(21,y),(19,y+2)],fill=c)
        d.rectangle((12,10,20,20),fill=DARK)
        d.rectangle((13,11,19,19),fill=shade(c,.75));d.line((13,11,18,11),fill=c)
        ring(d,16,15,3,WHITE)
    else:
        d.polygon([(16,2),(26,8),(29,18),(22,28),(9,28),(3,18),(6,8)],fill=DARK)
        d.line([(6,18),(8,9),(16,5),(24,9),(26,18),(21,25),(10,25),(6,18)],fill=STEEL,width=2)
        d.line([(7,18),(9,10),(16,6),(23,10)],fill=LIGHT)
        d.polygon([(16,7),(22,15),(16,23),(10,15)],fill=DARK)
        d.polygon([(16,8),(16,16),(11,15)],fill=shade(c,1.2))
        d.polygon([(16,8),(21,15),(16,16)],fill=c)
        d.polygon([(11,15),(16,16),(16,22)],fill=shade(c,.75))
        d.polygon([(16,16),(21,15),(16,22)],fill=shade(c,.5))
        for x,y in [(7,13),(24,13),(8,21),(23,21)]:
            d.line((x-1,y,x+1,y),fill=c);d.point((x,y-1),fill=WHITE)
    for i in range(5):
        x=9+3*i
        d.rectangle((x,24,x+1,25),fill=c if i<mk else DARK)
    return im


def fluid(route,mk):
    c=('#ac9cda' if route=='activator' else (TE if route=='technology' else MA)[mk-1])
    palette=[shade(c,k) for k in [.21,.32,.44,.58,.72,.87,1.0,1.15]]
    y,x=np.mgrid[:32,:32];frames=[]
    # Smooth periodic movement, quantized into eight crisp pixel tones like native fluids.
    for f in range(64):
        t=math.tau*f/64
        if route=='technology':
            field=np.sin(math.tau*(x/32+y/16)-t)+.6*np.cos(math.tau*y/8+t)+.35*np.sin(math.tau*x/8+2*t)
        else:
            field=np.sin(math.tau*x/16+1.3*np.sin(math.tau*y/32-t)+t)+.8*np.cos(math.tau*y/16-t)
        ix=np.clip(((field+2.0)/4.0*7).astype(int),0,7)
        a=np.asarray(palette,dtype=np.uint8)[ix]
        im=Image.fromarray(a).convert('RGBA')
        # Sparse high-tier sparks, moving periodically inside the fluid, never a global flash.
        d=ImageDraw.Draw(im)
        for k in range(mk):
            px=(k*11+f//4)%32;py=(k*7+int(3*math.sin(t+k)))%32
            d.point((px,py),fill=shade(c,1.35))
        frames.append(up(im))
    return frames


def cell_with_rank(liquid,route,mk):
    im=cell_frame(liquid);d=ImageDraw.Draw(im);palette=TE if route=='technology' else MA
    for k in range(5):
        x=22+4*k;d.rectangle((x,53,x+1,54),fill=palette[mk-1] if k<mk else DARK)
    return im


def main():
    OUT.mkdir(parents=True,exist_ok=True)
    records=[];images={}
    def save(key,name,frames,folder='items'):
        path=ASSETS/folder/(key+'.png')
        if len(frames)>1: write_strip(frames,path)
        else: frames[0].save(path,optimize=True)
        records.append(dict(key=key,name=name,path=str(path.relative_to(ROOT)),frames=len(frames),sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
        images[key]=frames
    save('component_activator','苯环活化剂',[up(activator())])
    save('catalyst_quantum','苯环量子催化剂',[up(catalyst())])
    for i,t in enumerate(TIERS): save('star_'+t,t.upper()+'之星',[up(star(i))])
    for route,zh in [('technology','科技'),('magic','魔法')]:
        for mk in range(1,6):
            save(f'core_{route}_mk{mk}',f'{zh}指数核心 Mk{mk}',[up(core(route,mk))])
            frames=fluid(route,mk)
            save(f'{route}_base_mk{mk}',f'{zh}底数基液 Mk{mk}',frames,'blocks/fluids')
            save(f'base_cell_{route}_mk{mk}',f'{zh}底数基液单元 Mk{mk}',[cell_with_rank(f,route,mk) for f in frames])
    save('uv_activator','UV苯活化液',fluid('activator',1),'blocks/fluids')
    rows=[['component_activator','catalyst_quantum','uv_activator'],
          ['star_'+t for t in TIERS[:7]],['star_'+t for t in TIERS[7:]],
          [f'core_technology_mk{m}' for m in range(1,6)], [f'core_magic_mk{m}' for m in range(1,6)],
          [f'base_cell_technology_mk{m}' for m in range(1,6)], [f'base_cell_magic_mk{m}' for m in range(1,6)],
          [f'technology_base_mk{m}' for m in range(1,6)], [f'magic_base_mk{m}' for m in range(1,6)]]
    names={r['key']:r['name'] for r in records}
    def contact(frame):
        im=Image.new('RGB',(1120,1135),'#17202c');d=ImageDraw.Draw(im)
        d.text((25,18),'苯燃料拓展 · 完整部件材质',font=font(25),fill=WHITE)
        d.text((25,56),'统一金属边框 / 苯环标识 / 等级触点 / 原生流体动画',font=font(15),fill='#9bb2c4')
        for row,keys in enumerate(rows):
            for col,key in enumerate(keys):
                x=20+col*157;y=98+row*114
                d.rectangle((x,y,x+147,y+104),fill='#283647',outline='#405268')
                sprite=images[key][frame%len(images[key])].resize((72,72),Image.Resampling.NEAREST)
                im.paste(sprite,(x+38,y+5),sprite)
                label=names[key].replace('底数基液单元','基液单元').replace('底数基液','基液')
                d.text((x+5,y+81),label,font=font(12),fill=WHITE)
        return im
    contact(0).save(OUT/'all-components.png')
    gif=[contact(f) for f in range(0,64,4)]
    gif[0].save(OUT/'all-components.gif',save_all=True,append_images=gif[1:],duration=400,loop=0,optimize=True)
    html=['<!doctype html><meta charset="utf-8"><title>苯燃料 · 部件材质</title><style>body{background:#17202c;color:#e9f5fa;font:16px sans-serif;margin:32px}main{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px}article{background:#283647;border:1px solid #405268;padding:12px;text-align:center}canvas{width:96px;height:96px;image-rendering:pixelated}button{padding:10px;margin:8px}</style><h1>部件材质预览</h1><p>35 种物品 + 11 种流体；图像来自实际游戏资源。</p><button onclick="paused=!paused;this.textContent=paused?\'继续动画\':\'暂停动画\'">暂停动画</button><main>']
    data=[]
    for r in records:
        payload=base64.b64encode((ROOT/r['path']).read_bytes()).decode()
        html.append(f'<article><canvas id="{r["key"]}" width="64" height="64"></canvas><p>{r["name"]}</p></article>')
        data.append(dict(id=r['key'],frames=r['frames'],src='data:image/png;base64,'+payload))
    html.append('</main><script>let paused=false,t=0;const sprites='+json.dumps(data)+';for(const s of sprites){s.img=new Image;s.img.src=s.src;s.ctx=document.getElementById(s.id).getContext("2d");}setInterval(()=>{if(!paused)t++;for(const s of sprites){if(!s.img.complete)continue;s.ctx.clearRect(0,0,64,64);s.ctx.drawImage(s.img,0,(t%s.frames)*64,64,64,0,0,64,64)}},100)</script>')
    (ROOT/'preview/components.html').write_text(''.join(html))
    (OUT/'manifest.json').write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n')
    print(f'{len(records)} sprites; {sum(r["frames"]>1 for r in records)} animations')

if __name__=='__main__': main()
