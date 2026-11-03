#!/usr/bin/env python3
"""Select verified GT5 block variants and extract native preview textures.

Requires Pillow. No game/client is launched. Only the single blue-white light casing
is derived artwork; native blocks keep their original texture pixels.
"""
import base64
import hashlib
import json
from io import BytesIO
from pathlib import Path
from zipfile import ZipFile

from PIL import Image, ImageDraw, ImageOps

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parents[1]
GT_JAR = WORKSPACE / 'work/gtnh-machine-validation/server/mods/gregtech-5.09.54.205.jar'
MC_JAR = ROOT / 'build/rfg/srg_merged_minecraft.jar'
gt, mc = ZipFile(GT_JAR), ZipFile(MC_JAR)
ICON = 'assets/gregtech/textures/blocks/iconsets/'
entries = {}
textures = {}


def native(role, name, item_list, block, meta, texture, source='gregtech'):
    resource = texture if '/' in texture else ICON + texture + '.png'
    raw = (mc if source == 'minecraft' else gt).read(resource)
    image = Image.open(BytesIO(raw)).convert('RGBA')
    # Animated PNG strips show the first frame in this static architecture preview.
    if image.height > image.width: image = image.crop((0, 0, image.width, image.width))
    path = ROOT / 'preview/textures' / (role + '.png')
    image.save(path)
    png = path.read_bytes()
    textures[role] = 'data:image/png;base64,' + base64.b64encode(png).decode()
    entries[role] = {'name_zh':name, 'status':'existing_block', 'item_list':item_list,
                     'registry_name':block, 'metadata':meta, 'texture_key':role,
                     'texture_resource':resource, 'source_mod':source,
                     'source_texture_sha256':hashlib.sha256(raw).hexdigest(),
                     'native_pixels_modified':False}


native('foundation','工业高强度混凝土','BlockIndustrialStrengthConcrete','gregtech:gt.blockcasings9',3,'INDUSTRIAL_STRENGTH_CONCRETE')
native('casing','聚变机械方块','Casing_Fusion','gregtech:gt.blockcasings4',6,'MACHINE_CASING_FUSION')
native('frame','坚固钢机械方块','Casing_SolidSteel','gregtech:gt.blockcasings2',0,'MACHINE_CASING_SOLID_STEEL')
native('armor','洁净不锈钢机械方块','Casing_CleanStainlessSteel','gregtech:gt.blockcasings4',1,'MACHINE_CASING_CLEAN_STAINLESSSTEEL')
native('shielding','化学惰性机械方块','Casing_Chemically_Inert','gregtech:gt.blockcasings8',0,'MACHINE_CASING_CHEMICALLY_INERT')
native('alloy','防冻机械方块','Casing_FrostProof','gregtech:gt.blockcasings2',1,'MACHINE_CASING_FROST_PROOF')
native('glass','量子玻璃',None,'tectech:tile.quantumGlass',0,'assets/tectech/textures/blocks/blockQuantumGlass.png')
native('heating_coil','加热线圈（默认白铜，可整层升级）','Casing_Coil_Cupronickel','gregtech:gt.blockcasings5',0,'MACHINE_COIL_CUPRONICKEL')
native('technology','超导线圈方块','Casing_Coil_Superconductor','gregtech:gt.blockcasings',15,'MACHINE_COIL_SUPERCONDUCTOR')
native('magic','魔法机械方块','Casing_Magical','gregtech:gt.blockcasings8',8,'MACHINE_CASING_MAGICAL')
native('conduit','聚四氟乙烯管道方块','Casing_Pipe_Polytetrafluoroethylene','gregtech:gt.blockcasings8',1,'MACHINE_CASING_PIPE_POLYTETRAFLUOROETHYLENE')
native('core','遏制场机械方块','Casing_ContainmentField','gregtech:gt.blockcasings2',8,'MACHINE_CASING_CONTAINMENT_FIELD')
native('coolant','冷却管道机械方块','Coolant_Duct_Casing','gregtech:gt.blockcasings10',9,'COOLANT_DUCT_CASING')
native('radiator','高级过滤机械方块','Casing_Vent_T2','gregtech:gt.blockcasings9',1,'MACHINE_CASING_VENT_T2')
native('grating','格栅机械方块','Casing_Grate','gregtech:gt.blockcasings3',10,'MACHINE_CASING_GRATE')
native('rail','铁栏杆',None,'minecraft:iron_bars',0,'assets/minecraft/textures/blocks/iron_bars.png','minecraft')
native('capacitor','LuV气密机械方块','Casing_Tank_6','gregtech:gt.blockcasings6',6,'MACHINE_CASING_TANK_6')
native('busbar','青铜齿轮箱机械方块','Casing_Gearbox_Bronze','gregtech:gt.blockcasings2',2,'MACHINE_CASING_GEARBOX_BRONZE')
native('display','数据存储机械方块','Casing_DataDrive','gregtech:gt.blockcasings2',7,'MACHINE_CASING_DATA_DRIVE')
native('antenna','铁栏杆阵列',None,'minecraft:iron_bars',0,'assets/minecraft/textures/blocks/iron_bars.png','minecraft')

# A genuinely luminous blue-white casing is the only added structural block.
source = ICON + 'MACHINE_DIM_INJECTOR.png'
image = Image.open(BytesIO(gt.read(source))).convert('RGB')
image = ImageOps.colorize(ImageOps.grayscale(image), (23,49,66), (209,241,249)).resize((32,32),Image.Resampling.NEAREST).convert('RGBA')
draw = ImageDraw.Draw(image)
draw.rectangle((0,0,31,31),outline=(79,111,131,255),width=2)
draw.rectangle((3,3,28,28),outline=(222,241,248,255),width=2)
for a,b in [((6,15),(25,15)),((15,6),(15,25))]:draw.line((a,b),fill=(88,220,246,255),width=3)
for x,y in [(5,5),(25,5),(5,25),(25,25)]:draw.rectangle((x,y,x+1,y+1),fill=(115,211,238,255))
path=ROOT/'src/main/resources/assets/benzenefuels/textures/blocks/resonance_light_casing.png'
image.save(path)
image.save(path.with_name('resonance_light_casing_active.png'))
from PIL import ImageEnhance
idle=ImageEnhance.Brightness(image).enhance(0.52)
idle.save(path.with_name('resonance_light_casing_idle.png'))
light='data:image/png;base64,'+base64.b64encode(path.with_name('resonance_light_casing_idle.png').read_bytes()).decode()
for role in ['light','beacon']:
    textures[role]=light
    textures[role+'_active']='data:image/png;base64,'+base64.b64encode(path.with_name('resonance_light_casing_active.png').read_bytes()).decode()
    entries[role]={'name_zh':'蓝白共振导光外壳','status':'new_block','item_list':None,
                   'registry_name':'benzenefuels:resonance_light_casing','metadata':0,'texture_key':role,
                   'texture_resource':'assets/benzenefuels/textures/blocks/resonance_light_casing.png',
                   'derived_from':source,'source_mod':'gregtech','native_pixels_modified':True,
                   'active_texture_key':role+'_active','light_level':15,'idle_light_level':0,'survival_recipe':'pending_with_generator_design'}
for role,item,name in [('fuel_port','Hatch_Input_LuV','LuV输入仓'),('tech_port','Hatch_Input_LuV','LuV输入仓（科技流体）'),
                       ('magic_port','Hatch_Input_LuV','LuV输入仓（魔法流体）'),('output_port','Hatch_Dynamo_LuV','LuV动力仓'),
                       ('maintenance','Hatch_Maintenance','维护仓')]:
    entries[role]={'name_zh':name,'status':'existing_mte','item_list':item,'metadata':None,
                   'registry_name':'gregtech:gt.blockmachines','tier_note':'LuV is the initial proposal; higher-tier variants remain selectable.'}
entries['controller']={'name_zh':'共振燃料发电塔主机','status':'existing_mte','item_list':None,
                       'registry_name':'gregtech:gt.blockmachines','metadata':32240}
controller_art=ROOT/'preview/controller'
if (controller_art/'controller_front_idle.png').exists():
    for name in ('side','front_idle','front_active'):
        textures['controller_'+name]='data:image/png;base64,'+base64.b64encode((controller_art/f'controller_{name}.png').read_bytes()).decode()
    entries['controller'].update({'texture_key':'controller_side','front_texture_key':'controller_front_idle',
        'active_front_texture_key':'controller_front_active','front_normal':[0,0,-1],
        'base_casing':'gregtech:gt.blockcasings8:0','art_status':'runtime_registered_generator'})
unique={(e['registry_name'],e['metadata']) for k,e in entries.items() if e['status'] in ['existing_block','new_block']}
palette={'target':'GTNH 2.9.0-RC-2 / GT5 5.09.54.205','status':'proposed_material_selection',
         'revision':'native-gtnh-materials','roles':entries,'texture_data_urls':textures,
         'unique_structural_blocks':len(unique),'new_structural_blocks':1,
         'notes':['Existing casings use their actual texture pixels, not recolored substitutes.',
                  'GT side textures represent the block in this architectural preview; special renderers/dyes/animations are not simulated.',
                  'Structure selection is not a generator crafting recipe or an unlock guarantee.']}
(ROOT/'design/generator-material-palette.json').write_text(json.dumps(palette,ensure_ascii=False,indent=2)+'\n')
runtime={k:v for k,v in palette.items() if k!='texture_data_urls'}
(ROOT/'src/main/resources/assets/benzenefuels/generator-palette.json').write_text(json.dumps(runtime,ensure_ascii=False,indent=2)+'\n')
(ROOT/'preview/TEXTURE-SOURCES.md').write_text('''# Texture sources

Native GregTech and TecTech textures are extracted without pixel changes from GT5-Unofficial 5.09.54.205 for the local structure preview.
Source: https://github.com/GTNewHorizons/GT5-Unofficial/tree/5.09.54.205/src/main/resources/assets
Project license: LGPL-3.0; the addon includes its LICENSE. Exact resource paths and SHA-256 values are recorded in design/generator-material-palette.json.

The new resonance_light_casing.png is derived from GregTech iconsets/MACHINE_DIM_INJECTOR.png: recolored to blue-white, scaled with nearest-neighbor filtering and given a frame, bolts and luminous cross-channel. Its editable generator is tools/select_structure_materials.py.

Minecraft iron-bar artwork is used solely by the local preview; the game uses the installed vanilla block. No vanilla texture is redistributed inside the addon JAR.

Controller art: tools/design_controller_texture.py draws 64x64 transparent idle/active front overlays and separate emissive masks. Its background is an unchanged nearest-neighbor enlargement of MACHINE_CASING_CHEMICALLY_INERT.png, preserving the outer 8px. OVERLAY_FRONT_DIESEL_ENGINE(_ACTIVE).png and LARGETURBINE_NEW(5/_ACTIVE5).png are visual references only; their controller graphics are not copied. Native casing remains the future side/top/back texture. Provenance and hashes: verification/controller-texture.json. The 0.3.0 controller uses these layered textures at runtime. Light casing metadata 0/1 select idle/active art and light values 0/15.
''')
print(json.dumps({'roles':len(entries),'unique_structural_blocks':len(unique),'existing_unique_structural_blocks':len(unique)-1,'new_blocks':1},ensure_ascii=False))
