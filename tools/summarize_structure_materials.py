#!/usr/bin/env python3
"""Count placed voxels, merging identical block IDs/metadata across roles."""
import csv
import json
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
m=json.loads((ROOT/'design/generator-structure.json').read_text())
palette=m['native_materials']
audit=json.loads((ROOT/'verification/native-material-palette.json').read_text())
counts=Counter(v[3] for v in m['voxels'])
assert dict(counts)==m['counts']
assert len({tuple(v[:3]) for v in m['voxels']})==len(m['voxels'])
assert sum(counts.values())==len(m['voxels'])==15472
grouped={}
for role,n in counts.items():
    e=palette[role];actual=audit.get(role,{})
    registry=actual.get('registry',e.get('registry_name'))
    meta=actual.get('metadata',e.get('metadata'))
    key=(registry,meta) if registry else ('reserved:'+role,None)
    if key not in grouped:
        grouped[key]={'name_zh':e['name_zh'],'name_en':actual.get('displayName','Resonance Fuel Generator'),
                      'registry_name':registry,'metadata':meta,'quantity':0,'roles':[],
                      'status':e['status']}
    grouped[key]['quantity']+=n
    grouped[key]['roles'].append(role)
rows=sorted(grouped.values(),key=lambda r:(r['status'] in ('existing_mte','reserved_controller'),-r['quantity']))
for r in rows:
    q=r['quantity'];r['groups_of_64']=q//64;r['remainder']=q%64
    r['stacks_64_equivalent']=(q+63)//64
total=sum(r['quantity'] for r in rows)
assert total==15472 and len(rows)==24
report={'revision':m['revision'],'dimensions':m['dimensions'],'total_placed_blocks':total,
        'structural_blocks':total-6,'controller_and_hatches':6,'distinct_entries':len(rows),
        'structural_variants':20,'existing_structural_variants':19,'new_structural_variants':1,
        'delta_from_previous':{'total_blocks':1632,'heating_coil':1648,'frame':-8,'conduit':-8},
        'controller_coordinate':[0,2,-16],'controller_front_normal':[0,0,-1],
        'scope':'Placed structure including full foundation and decoration; excludes air, recipes, tools, cables outside structure and controller inventory contents.',
        'stack_note':'64-item groups are arithmetic equivalents, not a guarantee for unregistered controller stack size.',
        'materials':rows}
(ROOT/'design/generator-bom.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
with (ROOT/'design/generator-bom.csv').open('w',encoding='utf-8-sig',newline='') as f:
    writer=csv.writer(f);writer.writerow(['方块','数量','64个/组','余数','注册名','元数据','结构用途'])
    for r in rows:writer.writerow([r['name_zh'],r['quantity'],r['groups_of_64'],r['remainder'],r['registry_name'],r['metadata'],','.join(r['roles'])])
lines=['# 发电机完整用料 / Generator bill of materials','',
       '**33×33×129，共15,472个方块，24种用料条目。** 包含全部地基、内部核心和装饰，不含空气、外接管线及主机内部物品；这是搭建用量，不是合成这些方块所需的原矿展开。','',
       '20种结构方块变体＋3种原生仓室＋1个主机。相同注册名和元数据已合并：导光外壳616＋信标5＝621；栏杆60＋天线20＝80；三路LuV输入仓合计3个。','',
       '入口面板已全部补齐到Z=-16；维护仓移到前侧，动力仓移到后侧两排钢外壳。中央立柱Y=8～110外包5×5截面的加热线圈护套，共1,648个。替换8个框架和8个导流方块，净增1,632格；全层须使用同级线圈。清单按默认3输入、1维护、1动力仓计算；额外动力仓每增加1个，减少1个后侧坚固钢机械方块。', '',
       '| 材料 | 数量 | 64个/组换算 | 注册名:元数据 |','|---|---:|---|---|']
for r in rows:
    stack=(str(r['groups_of_64'])+'组' if r['groups_of_64'] else '')+('＋' if r['groups_of_64'] and r['remainder'] else '')+(str(r['remainder'])+'个' if r['remainder'] else '')
    identity=f"{r['registry_name']}:{r['metadata']}" if r['registry_name'] else '待实现'
    lines.append(f"| {r['name_zh']} | {r['quantity']:,} | {stack} | `{identity}` |")
lines+=['','默认输入／动力仓按LuV选型，可使用其他等级。主机已注册；最多3个输入仓、1个维护仓位于前侧惰性外壳Y=2或3，最底排Y=1禁止放仓室。后侧Z=14、Y=2或3的两排坚固钢外壳支持0～16个动力仓。64个/组是数量换算。','',
        '主机坐标(0,2,-16)，正面朝-Z；周围8格同层化学惰性机械方块。前景保留原生边框，侧／顶／背采用同种底壳；正面提供停机／运行／独立发光覆盖层。主机与621个导光外壳均按运行状态切换材质。','',
        'Machine controller textures use the native casing background and preserve its border pixels. The 1648-coil jacket replaces 16 existing blocks and adds 1632 placed blocks without changing tower dimensions. Counts merge identical registry names and metadata. No client was launched.']
(ROOT/'design/generator-materials.md').write_text('\n'.join(lines)+'\n')
print(json.dumps({'total':total,'entries':len(rows),'materials':[{k:r[k] for k in ('name_zh','quantity')} for r in rows]},ensure_ascii=False))
