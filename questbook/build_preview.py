#!/usr/bin/env python3
"""Build a reviewable chapter map from the exact authored BQ definitions (no dependencies)."""
import json
import uuid
from pathlib import Path

import build_chapter as chapter

HERE = Path(__file__).resolve().parent
chapter.validate()
ids = {chapter.uid(row['key']): row['key'] for row in chapter.QUESTS}
ids.update({uuid.UUID(int=q): f'native_{q}' for q, _, _ in chapter.ANCHORS})
rows = []
shorts = {
    'welcome': ('Greg，炼苯了', 'Greg, cook!'), 'platinum_activator': ('五等分贵金属', 'Quintuplets'),
    'uv_activator': ('UV 活化液', 'UV activator'), 'quantum_catalyst': ('催化剂不败', 'Undefeated'),
    'gas_generators': ('燃气发电', 'Gas power'), 'automation': ('产线自动化', 'Automation'),
    'tower': ('Benzene, son!', 'Benzene, son!'), 'tower_materials': ('未闻外壳数', 'Ano-casings'),
    'tower_coils': ('线圈并行', 'Coils'), 'tower_hatches': ('仓室位置', 'Hatches'),
    'tower_operation': ('燃料投喂', 'Operation'), 'tower_output': ('你的电，我的电', 'Your power'),
    'technology_route': ('遥遥领先', 'Far ahead'), 'magic_route': ('科学尽头是玄学', 'Beyond science'),
    'stars': ('电压之星', 'Voltage stars')}
tiers = ['LV','MV','HV','EV','IV','LuV','ZPM','UV','UHV','UEV','UIV','UMV','UXV']
for row in chapter.QUESTS:
    key=row['key']; quest=row['quest']; entry=row['entry']; props=quest['properties:10']['betterquesting:10']
    parents=[]
    for parent in quest['preRequisites:9'].values():
        value=uuid.UUID(int=((parent['questIDHigh:4']&((1<<64)-1))<<64)|(parent['questIDLow:4']&((1<<64)-1)))
        parents.append(ids[value])
    short=shorts.get(key)
    art='';symbol='•'
    if key.startswith('fuel_'):
        tier=key[5:];short=(tier.upper(),tier.upper());art='cell_'+tier
    elif key.startswith('star_'):
        tier=tiers[int(key[5:])];short=(tier,tier);art='star_'+tier.lower()
    elif '_mk' in key:
        route,mk=key.split('_mk');short=('Mk'+mk,'Mk'+mk);art='core_'+route+'_mk'+mk
    elif key in ('technology_route','magic_route'):
        art='core_'+key.split('_')[0]+'_mk1'
    elif key=='platinum_activator':art='component_activator'
    elif key=='quantum_catalyst':art='catalyst_quantum'
    elif key=='welcome':art='cell_luv'
    elif key=='stars':art='star_lv'
    elif key.startswith('tower'):symbol={'tower':'UV','tower_materials':'▦','tower_coils':'Ω','tower_hatches':'I/O','tower_operation':'5s','tower_output':'EU'}[key]
    if art:
        path=HERE.parent/'src/main/resources/assets/benzenefuels/textures/items'/f'{art}.png'
        if not path.exists():
            art=''
        else:
            art='../src/main/resources/assets/benzenefuels/textures/items/'+path.name
    output={'key':key,'x':entry['x:3'],'y':entry['y:3'],'size':entry['sizeX:3'],
            'parents':parents,'kind':'magic' if key.startswith('magic') else 'major' if key.startswith('fuel_') else 'info',
            'symbol':symbol,'art':art,'short':dict(zip(['zh_CN','en_US'],short))}
    prefix='betterquesting.quest.'+chapter.encoded(chapter.uid(key)).rstrip('=')
    task=quest['tasks:9']['0:10']; task_id=task['taskID:8']
    if task_id.endswith('checkbox'):task_labels=('阅读后勾选；无强制材料提交。','Read and check the box; no required materials.')
    elif task_id.endswith('fluid'):
        amount=task['requiredFluids:9']['0:10']['Amount:3'];task_labels=(f'检测 {amount:,}mB 流体，不消耗。',f'Detect {amount:,} mB in containers, without consumption.')
    else:task_labels=('检测指定物品，不消耗。','Detect the specified item without consuming it.')
    rewards=quest['rewards:9']
    if rewards:
        reward=rewards['0:10']['rewards:9']['0:10'];name=reward['id:8'].split('Coin')[1];count=reward['Count:3']
        reward_labels=(f'一次性奖励：{count} × '+name.replace('Chemist','化学家硬币 ').replace('Technician','技术员硬币 '),f'One-time reward: {count} × {name} coin')
    else:reward_labels=('无物资奖励。','No material reward.')
    for i,lang in enumerate(['zh_CN','en_US']):
        output[lang]={'name':chapter.TEXT[lang][prefix+'.name'],'desc':chapter.TEXT[lang][prefix+'.desc'],
                      'task':task_labels[i],'reward':reward_labels[i]}
    rows.append(output)
native_names={3133:('硝基苯','Nitrobenzene'),1412:('装配线','Assembly Line'),1212:('聚变 MkI','Starting Fusion'),
              1197:('聚变 MkII','Fusion MkII'),1202:('聚变 MkIII','Fusion MkIII'),2959:('硅岩精炼厂','Naquadah Refinery'),3287:('量子力变换机','QFT')}
for value,x,y in chapter.ANCHORS:
    row={'key':f'native_{value}','x':x,'y':y,'size':24,'parents':[],'kind':'native','art':'','symbol':'GT',
         'short':dict(zip(['zh_CN','en_US'],native_names[value]))}
    for i,lang in enumerate(['zh_CN','en_US']):
        row[lang]={'name':native_names[value][i],'desc':('引用原版 GTNH 任务。其要求、奖励与玩家进度保持不变；不是新增的重复任务。' if i==0 else 'Reference to the original GTNH quest. Requirements, rewards and progress remain unchanged; this is not a duplicate quest.'),
                  'task':f'GTNH quest ID: {value}','reward':('按原版任务发放，不额外发奖。' if i==0 else 'Original rewards only, no additional claim.')}
    rows.append(row)
template=(HERE/'preview.template.html').read_text()
template=template.replace("{y:168,zh:'燃料主线',en:'Fuel progression'}, ",'').replace('{y:472,','{y:488,').replace('{y:568,','{y:584,')
(HERE/'preview.html').write_text(template.replace('__QUEST_DATA__',json.dumps(rows,ensure_ascii=False).replace('</','<\\/')))
print('Wrote preview.html from 53 native-layout nodes')
