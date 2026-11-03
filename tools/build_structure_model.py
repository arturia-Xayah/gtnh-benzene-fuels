#!/usr/bin/env python3
"""Generate the proposed 33 x 33 x 129 cloud-piercing reactor tower."""
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
grid = {}


def block(x, y, z, kind):
    assert -16 <= x <= 16 and -16 <= z <= 16 and 0 <= y <= 128
    grid[x, y, z] = kind


def ring(y, radius, kind, thickness=1.25):
    extent = int(radius + 1)
    for x in range(-extent, extent + 1):
        for z in range(-extent, extent + 1):
            distance = (x*x + z*z)**0.5
            if radius-thickness <= distance <= radius+0.35:
                block(x, y, z, kind)


# Monumental chamfered plinth. Its service apron defines the 33-block footprint.
for x in range(-16, 17):
    for z in range(-16, 17):
        if abs(x)+abs(z) <= 27:
            block(x, 0, z, 'foundation')
for y in range(1, 8):
    radius = 14 - (y-1)//3
    for x in range(-radius, radius+1):
        for z in range(-radius, radius+1):
            if abs(x)+abs(z) > radius*2-5:
                continue
            if y == 1 or y == 7 or max(abs(x), abs(z)) == radius:
                block(x, y, z, 'casing' if y in (1,7) else 'frame')
# Broad corner buttresses taper toward a slender shaft, rather than a tall box.
for sx, sz in [(-1,-1),(-1,1),(1,-1),(1,1)]:
    for y in range(1, 109):
        radius = 12 if y < 8 else 11 if y < 16 else 10 if y < 28 else 9 if y < 96 else 8
        for dx,dz in [(0,0),(sx,0),(0,sz)]:
            block(sx*radius+dx, y, sz*radius+dz, 'casing')
        if y%8 == 0:
            for t in range(6, radius+1):
                block(sx*t, y, sz*t, 'frame')
# A translucent cylindrical chamber, restrained horizontal bands, central reaction axis.
for y in range(8, 111):
    ring(y,6,'frame' if y%16 in (0,1) else 'glass',0.7)
    for x,z in [(0,0),(1,0),(-1,0),(0,1),(0,-1)]:
        block(x,y,z,'core' if y%4 else 'conduit')
    for x,z in [(7,0),(-7,0),(0,7),(0,-7)]:
        block(x,y,z,'conduit')
# Ring decks separate the technology and magic directions on the common spine.
for y,kind in [(34,'technology'),(78,'magic')]:
    ring(y-1,12,'frame',1.8)
    ring(y,12,kind,1.8)
    ring(y+1,12,kind,1.8)
    ring(y+2,12,'casing',1.8)
    for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
        for t in range(1,12):
            block(dx*t,y,dz*t,'conduit')
            block(dx*t,y-1,dz*t,'frame')
# Four long outer blades tie the ring decks to the tapering crown.
for y in range(12, 111):
    r=14 if y < 20 else 13 if y < 28 else 12 if y < 88 else 11 if y < 96 else 10 if y < 104 else 8
    for x,z in [(r,0),(-r,0),(0,r),(0,-r)]:
        block(x,y,z,'casing')
        if y in (16, 28, 36, 56, 80, 96, 104):
            if x: block(x,y,z+1,'frame');block(x,y,z-1,'frame')
            else: block(x+1,y,z,'frame');block(x-1,y,z,'frame')
# Open eight-sided crown narrows into an illuminated needle, highest point at Y=128.
for y in range(109, 127):
    r=max(1,8-(y-109)//2)
    for dx,dz in [(1,0),(-1,0),(0,1),(0,-1),(1,1),(-1,1),(1,-1),(-1,-1)]:
        block(dx*r,y,dz*r,'casing' if y%2 else 'conduit')
    if y in (110,114,118,122): ring(y,r,'frame',0.8)
for y in range(111,129): block(0,y,0,'core' if y>=125 else 'conduit')

# Segmented ceramic armor and inset shielding break up the uninterrupted white piers.
for (x,y,z),kind in list(grid.items()):
    if kind == 'casing' and 8 <= y <= 108:
        phase=y%16
        grid[x,y,z]='alloy' if phase in (0,1) else 'shielding' if phase in (2,15) else 'armor'
    elif kind == 'conduit' and 8 <= y <= 110 and (abs(x)==7 or abs(z)==7):
        grid[x,y,z]='coolant'
    elif kind == 'frame' and y in (16,32,48,64,80,96):
        grid[x,y,z]='alloy'

# Recessed light rails run beside the principal four blades, with dark expansion joints.
for y in range(12,109):
    r=14 if y<20 else 13 if y<28 else 12 if y<88 else 11 if y<96 else 10 if y<104 else 8
    for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
        if y%16 not in (0,1,2): block(dx*(r+1),y,dz*(r+1),'light')
        if y%16 in (3,4,5,6,7,8):
            for tangent in (-1,1):
                block(dx*r-dz*tangent,y,dz*r+dx*tangent,'radiator')

# Grated inspection galleries and intermittent guardrails make the silhouette layered.
for y,radius in ((14,13),(36,14.5),(56,10),(80,14.5),(102,10)):
    ring(y,radius,'grating',1.35)
    for x in range(-15,16):
        for z in range(-15,16):
            d=(x*x+z*z)**0.5
            if radius-0.3 <= d <= radius+0.35 and (abs(x)+abs(z))%3==0:
                block(x,y+1,z,'rail')
            if radius-1.35 <= d <= radius+0.35 and (abs(x)+abs(z))%5==0:
                block(x,y,z,'light')

# Four radial cooling/capacitor pods at three elevations, with ribbed outward faces.
for base_y in (18,50,94):
    for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
        for r in range(11,15):
            for tangent in range(-2,3):
                for dy in range(5):
                    if r not in (11,14) and abs(tangent)!=2 and dy not in (0,4): continue
                    kind='armor' if dy in (0,4) or abs(tangent)==2 else 'capacitor'
                    if r==14 and dy in (1,2,3): kind='radiator'
                    block(dx*r-dz*tangent,base_y+dy,dz*r+dx*tangent,kind)
        for dy in range(1,4): block(dx*15,base_y+dy,dz*15,'coolant')
        for tangent in (-2,2): block(dx*14-dz*tangent,base_y+4,dz*14+dx*tangent,'light')

# Ring power taps: bronze busbars terminate in a blue or violet capacitor block.
for y,route in ((34,'technology'),(78,'magic')):
    for sx,sz in ((-1,-1),(-1,1),(1,-1),(1,1)):
        for dy in (-1,0,1):
            block(sx*9,y+dy,sz*9,'capacitor' if dy else route)
            block(sx*10,y+dy,sz*9,'alloy')
        block(sx*8,y,sz*8,'busbar')
        block(sx*9,y+2,sz*9,'light')
    ring(y+1,13,'light',0.55)

# Base cooling banks, illuminated paths, and a tiled rather than featureless apron.
for (x,y,z),kind in list(grid.items()):
    if y==0 and kind=='foundation':
        if abs(x)%8==0 or abs(z)%8==0: grid[x,y,z]='shielding'
        elif (abs(x)+abs(z))%8==0: grid[x,y,z]='alloy'
for sx in (-1,1):
    for z in range(-7,8):
        for y in range(2,6): block(sx*15,y,z,'radiator' if z%3 else 'alloy')
    for z in range(-13,14):
        if abs(z)>8: block(sx*14,1,z,'light')
for x in range(-7,8):
    for z in range(-14,-9): block(x,1,z,'grating')

# Crown emitter array and ice-blue beacon cap, still below the agreed Y=128 limit.
for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
    for y in range(112,118):
        for tangent in range(-1,2):
            r=7 if y<115 else 6
            block(dx*r-dz*tangent,y,dz*r+dx*tangent,'display' if abs(tangent)==0 else 'alloy')
    for y in range(119,124):block(dx*4,y,dz*4,'antenna')
for y in range(124,129):block(0,y,0,'beacon')
# Front operations wall; all connectors remain accessible at ground level.
for x in range(-7,8):
    for y in range(1,5):
        block(x,y,-15,'alloy' if y==4 else 'shielding')
        block(x,y,-16,'alloy' if y==4 else 'shielding')
for x in (-5,-4,-1,0,1,4,5): block(x,3,-16,'display')
ports=[
    {'coordinate':[0,2,-16], 'kind':'controller', 'label':'主控制器 / 指数核心槽'},
    {'coordinate':[-6,2,-16], 'kind':'fuel_port', 'label':'燃料输入仓'},
    {'coordinate':[-3,2,-16], 'kind':'tech_port', 'label':'科技底数流体输入仓'},
    {'coordinate':[3,2,-16], 'kind':'magic_port', 'label':'魔法底数流体输入仓'},
    {'coordinate':[0,2,14], 'kind':'output_port', 'label':'后侧动力仓（两排钢外壳内，最多16个）'},
    {'coordinate':[6,2,-16], 'kind':'maintenance', 'label':'前侧维护仓'},
]
for p in ports:block(*p['coordinate'],p['kind'])
# Native inert casings surround the flush controller.
for x in (-1,0,1):
    for y in (1,2,3):
        if (x,y)!=(0,2):block(x,y,-16,'shielding')
# Functional heating jacket: 5x5 perimeter around the central five-block axis.
# Applied after decks so the jacket remains continuous, Y=8..110 inclusive.
for y in range(8,111):
    for x in range(-2,3):
        for z in range(-2,3):
            if max(abs(x),abs(z)) == 2: block(x,y,z,'heating_coil')
assert sum(k == 'heating_coil' for k in grid.values()) == 1648
voxels=[[*point,kind] for point,kind in sorted(grid.items())]
assert [max(p[a] for p in grid)-min(p[a] for p in grid)+1 for a in range(3)] == [33,129,33]
assert len({tuple(v[:3]) for v in voxels}) == len(voxels)
assert all(grid[tuple(p['coordinate'])] == p['kind'] for p in ports)
labels={'foundation':'基础底座','casing':'结构外壳','frame':'承重框架','glass':'观察玻璃',
        'heating_coil':'中央加热线圈护套','technology':'科技增益环','magic':'魔法增益环','conduit':'能量导流柱','core':'反应核心',
        'controller':'主控制器','fuel_port':'燃料输入','tech_port':'科技流体输入',
        'magic_port':'魔法流体输入','output_port':'输出接口','maintenance':'维护仓'}
labels.update({'armor':'白色陶瓷装甲','shielding':'深色屏蔽层','alloy':'钛合金加固框',
    'coolant':'蓝色冷却管线','light':'发光能量导管','radiator':'散热鳍片格栅',
    'grating':'格栅检修平台','rail':'检修护栏','capacitor':'储能电容模块',
    'busbar':'镀金导能母排','display':'全息控制面板','antenna':'相控阵天线','beacon':'塔冠信标晶体'})
model={
    'name':'云霄双环共振塔', 'status':'runtime_generator',
    'dimensions':{'width_x':33,'length_z':33,'height_y':129},
    'origin':'center of foundation; front is negative Z; Y starts at 0',
    'materials':labels, 'counts':dict(Counter(grid.values())), 'ports':ports,'voxels':voxels,
    'revision':'technology-decoration',
    'material_profiles':{
        'grating':{'height':0.28},'rail':{'inset_xz':0.34,'height':0.8},
        'coolant':{'inset_xz':0.18},'light':{'inset_xz':0.18,'emissive':True},
        'antenna':{'inset_xz':0.28},'beacon':{'emissive':True},
        'armor':{'face_detail':'panel'},'radiator':{'face_detail':'fins'},
        'display':{'face_detail':'screen'},'capacitor':{'face_detail':'panel'},
    },
    'notes':['All materials are placeholder categories, not final GTNH block requirements.',
             'User requested a cloud-piercing tower rather than the initial low hall.',
             '33 x 33 plinth, 129-block illuminated spire, technology deck Y34 and magic deck Y78.',
             'Segmented armor, shielding, radiators, cooling pods, emissive rails, galleries and crown arrays.',
             'Used by the registered generator structure definition.'],
}
palette_path=ROOT/'design/generator-material-palette.json'
if palette_path.exists():
    palette=json.loads(palette_path.read_text())
    model['material_roles']=dict(model['materials'])
    model['materials']={k:palette['roles'][k]['name_zh'] for k in model['materials']}
    model['native_materials']=palette['roles']
    model['texture_data_urls']=palette['texture_data_urls']
    model['revision']='runtime-generator-heating-jacket'
    model['notes'][0]='Structural roles map to selected native block variants and one custom light casing.'
    model['notes'].append('Front inert casing plane Z=-16: input/maintenance hatches only at Y=2 or 3. Back steel plane Z=14: dynamos only at Y=2 or 3. Max 3 fluid inputs and 16 dynamos.')
    # Full GT casing cubes replace the earlier conceptual thin slabs and pipes.
    for kind in ('grating','coolant'):
        model['material_profiles'][kind]={}
    model['material_profiles']['light']={'emissive':True}
    model['material_profiles']['rail']={'inset_xz':0.34,'height':1}
(ROOT/'design'/'generator-structure.json').write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n')
runtime={'dimensions':model['dimensions'],'voxels':voxels,'materials':{k:{f:v for f,v in e.items() if f in ('registry_name','metadata','status')} for k,e in model.get('native_materials',{}).items()}}
(ROOT/'src/main/resources/assets/benzenefuels/generator-structure-runtime.json').write_text(json.dumps(runtime,separators=(',',':'))+'\n')
preview=ROOT/'preview'/'generator-structure.html'
text=preview.read_text()
before,rest=text.split('<!-- MODEL START -->',1)
_,after=rest.split('<!-- MODEL END -->',1)
data='<script type="application/json" id="reactor-structure-data">'+json.dumps(model,ensure_ascii=False,separators=(',',':'))+'</script>'
preview.write_text(before+'<!-- MODEL START -->\n'+data+'\n<!-- MODEL END -->'+after)
print(json.dumps({'dimensions':model['dimensions'],'blocks':len(voxels),'counts':model['counts']},ensure_ascii=False))
