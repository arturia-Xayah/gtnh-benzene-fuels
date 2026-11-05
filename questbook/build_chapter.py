#!/usr/bin/env python3
"""Author native BetterQuesting 3.8.92 data; regenerate with Python 3, no packages."""
import base64
import copy
import json
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NS = uuid.UUID('a33e5c49-075f-4ed5-b0d2-c7505d348afe')
QUESTS = []
TEXT = {'en_US': {}, 'zh_CN': {}}
ANCHORS = [(3133, 0, 72), (1412, 96, 0), (1212, 192, 0), (1197, 288, 0),
           (1202, 384, 0), (2959, 480, 0), (3287, 576, 0)]


def uid(key):
    return uuid.uuid5(NS, key)


def encoded(value):
    return base64.urlsafe_b64encode(value.bytes).decode()


def identity(value, prefix='questID'):
    return {prefix+'High:4': int.from_bytes(value.bytes[:8], 'big', signed=True),
            prefix+'Low:4': int.from_bytes(value.bytes[8:], 'big', signed=True)}


def stack(name, damage=0, count=1):
    return {'id:8': name, 'Damage:2': damage, 'Count:3': count, 'OreDict:8': ''}


def component(meta, count=1):
    return stack('benzenefuels:components', meta, count)


def indexed(values):
    return {str(i)+':10': value for i, value in enumerate(values)}


def retrieval(items, optional=False):
    return {'taskID:8': 'bq_standard:optional_retrieval' if optional else 'bq_standard:retrieval',
            'index:3': 0, 'autoConsume:1': 0, 'consume:1': 0, 'groupDetect:1': 0,
            'ignoreNBT:1': 1, 'partialMatch:1': 1, 'requireOnlyOneItem:1': 0,
            'requiredItems:9': indexed(items)}


def fluid(name, amount):
    return {'taskID:8': 'bq_standard:fluid', 'index:3': 0, 'autoConsume:1': 0,
            'consume:1': 0, 'groupDetect:1': 0, 'ignoreNBT:1': 1,
            'requiredFluids:9': indexed([{'FluidName:8': name, 'Amount:3': amount}])}


def coin(job, count=1, tier='III'):
    return stack('dreamcraft:Coin'+job+tier, 0, count)


def add(key, en, zh, en_text, zh_text, x, y, icon, parents=(), tasks=None, rewards=(), main=False):
    qid = uid(key)
    tasks = copy.deepcopy(tasks if tasks is not None else [{'taskID:8': 'bq_standard:checkbox'}])
    for i, task in enumerate(tasks):
        task['index:3'] = i
    props = {'name:8': en, 'desc:8': en_text, 'icon:10': icon, 'isMain:1': int(main),
             'questLogic:8': 'AND', 'taskLogic:8': 'AND', 'visibility:8': 'NORMAL',
             'repeatTime:3': -1, 'repeat_relative:1': 1, 'autoClaim:1': 0,
             'lockedProgress:1': 0, 'simultaneous:1': 0, 'globalShare:1': 0,
             'isSilent:1': 0, 'countAsQuest:1': int(tasks[0]['taskID:8'] != 'bq_standard:checkbox'),
             'snd_complete:8': 'random.levelup', 'snd_update:8': 'random.levelup'}
    q = {**identity(qid), 'properties:10': {'betterquesting:10': props},
         'preRequisites:9': indexed([identity(uuid.UUID(int=p) if isinstance(p, int) else uid(p)) for p in parents]),
         'tasks:9': indexed(tasks), 'rewards:9': indexed([{'index:3': 0, 'rewardID:8': 'bq_standard:item',
                       'ignoreDisabled:1': 0, 'rewards:9': indexed(list(rewards))}]) if rewards else {}}
    QUESTS.append({'key': key, 'quest': q, 'entry': {**identity(qid), 'x:3': x, 'y:3': y,
                                                  'sizeX:3': 32 if main else 24, 'sizeY:3': 32 if main else 24}})
    for lang, name, desc in [('en_US', en, en_text), ('zh_CN', zh, zh_text)]:
        prefix = 'betterquesting.quest.'+encoded(qid).rstrip('=')
        TEXT[lang][prefix+'.name'] = name
        TEXT[lang][prefix+'.desc'] = desc


add('welcome', 'Benzene Has Left the Building', '苯，未完待续',
    "You thought Nitrobenzene was the end of the line? Your tree farm has other plans. This chapter takes that familiar fuel through eight upgrades, from LuV to UXV. Every step keeps the same amount of fuel, but asks for increasingly expensive processing and additives.\n\nFollow the upper row for the fuel chain. The lower branches cover the Resonance Fuel Generator, its two upgrade routes, and Voltage Stars. Existing GTNH quests above the fuel row are real prerequisites, not additional copies to complete.\n\n[note]Fluid tasks detect containers in your inventory without consuming them. GT recipes call 1 mB one litre; this generator's parallel setting uses 1000 mB = 1 B. All recipe quantities here are written in mB to keep the two conventions apart.[/note]",
    "你以为硝基苯就是终点了？你的树场可不这么想。本章把这条熟悉的燃料线继续延伸，从 LuV 一路走到 UXV。每次升级都保留原有燃料体积，但加工设备和添加物会越来越昂贵。\n\n沿上方主线升级燃料，下方支线介绍共振燃料发电塔、科技与魔法强化，以及电压之星。主线上方的原版任务是真正的前置，不需要重复做一遍。\n\n[note]流体任务检测背包中的流体容器，不会收走燃料。GT 配方把 1mB 称为 1L，而本机的并行设置以 1000mB＝1B 计量。本章配方统一写 mB，别把两种口径混在一起。[/note]",
    0, 144, component(1), main=True)

add('platinum_activator', '§d§lA Very Noble Expense', '§d§l贵金属，贵有贵的道理',
    "Start with the Assembly Line and a steady supply of Helium Plasma. Scan Platinum Dust, then combine 1024 dust each of Platinum, Rhodium, Palladium, Iridium and Ruthenium with 16,384 mB of Helium Plasma. The line runs at LuV for 10 seconds to make one activator.\n\nThis little component is consumed. Each one upgrades 50,000 mB of Nitrobenzene, so an unautomated platinum-group line is going to make this fuel rather less renewable than your tree farm suggests.\n\n[note]Detect the activator before sending it to the reactor. This task does not consume it.[/note]",
    "先准备装配线和稳定的氦等离子体供应。扫描铂粉后，将铂、铑、钯、铱、钌粉各 1024 个，与 16,384mB 氦等离子体送入装配线，以 LuV 电压加工 10 秒，得到一个活化剂。\n\n这个小东西会被消耗。每个活化剂能升级 50,000mB 硝基苯，所以如果铂族处理线还靠手动搬运，你的“可再生能源”恐怕不会太轻松。\n\n[note]把活化剂送进反应釜之前，先在背包里检测一下。任务不会收走它。[/note]",
    192, 72, component(0), ('welcome', 3133, 1412, 1212),
    [retrieval([component(0)])], [coin('Chemist', 1)])

FUELS = [
 ('luv', 'LuV', 'Hyper-Enriched Platinum-Group Benzene', '超富集铂族苯', 13940, '§d§l', 192,
  ['platinum_activator'],
  'Feed one Platinum-Group Activator and 50,000 mB of Nitrobenzene into a Large Chemical Reactor. Ten seconds at LuV gives 50,000 mB of fuel. The activator is consumed; there is no second plasma payment at this step.\n\nYour tree farm now has a precious-metal dependency. Keep the activator production separate from the fuel buffer so a temporary shortage does not drain the fuel already available to your generators.',
  '将一个铂族活化剂和 50,000mB 硝基苯送入大型化学反应釜，以 LuV 电压加工 10 秒，得到等体积燃料。这一步会消耗活化剂，不会再收一次等离子体。\n\n你的树场现在有了贵金属供应商。把活化剂生产和燃料缓冲分开，别让一次短缺把发电机已有的口粮也抽走。'),
 ('zpm', 'ZPM', 'ZPM Benzene', 'ZPM苯', 136200, '§b§l', 288,
  ['fuel_luv', 1197],
  'Fuse 50,000 mB of LuV fuel with 163,840 mB of Tin Plasma. The recipe takes 10 seconds at ZPM and needs 280M EU of startup energy, so the ordinary Fusion Reactor must be MkII or better.\n\nNo Americium, Tritanium or Europium dust is used here. Tin Plasma is the entire additive bill, and it is a large one. Buffer enough for a complete recipe before feeding the reactor.',
  '将 50,000mB LuV 燃料与 163,840mB 锡等离子体聚变，得到 50,000mB ZPM 苯。配方耗时 10 秒，电压 ZPM，启动能量 280M EU，因此普通聚变反应堆需要 MkII 或更高。\n\n这里不需要镅粉、三钛粉或铕粉。全部添加物就是锡等离子体，而且量可不小。先攒够一整批，再往反应堆送料。'),
 ('uv', 'UV', 'Neutron-Supersaturated Benzene', '中子过饱和苯', 1491000, '§2§l§n', 384,
  ['uv_activator', 1202],
  'Send 50,000 mB of ZPM Benzene and 144 mB of UV Activator into fusion. This is a 10-second UV recipe with a 500M EU startup requirement. A normal MkIII reactor can handle it.\n\nThis is still a gas-turbine fuel. Higher energy density does not mean that an old dynamo suddenly has more throughput, though. Check the fuel flow and the output rating before scaling the line.',
  '将 50,000mB ZPM 苯和 144mB UV 活化液送入聚变反应堆。配方以 UV 电压运行 10 秒，启动能量 500M EU，普通 MkIII 聚变反应堆能够处理。\n\n它仍然属于燃气轮机燃料。不过，热值变高并不会让旧动力仓凭空多出吞吐量。扩产之前，先检查燃料流量和输出额定值。'),
 ('uhv', 'UHV', 'UHV Benzene', 'UHV苯', 18290000, '§4§l§n', 480,
  ['fuel_uv', 2959],
  'The Naquadah Fuel Refinery has found a second job. Combine 50,000 mB of UV fuel, 1024 Infinity Catalyst Dust, 1024 Atomic Separation Catalyst Dust and 17,856 mB of Molten Infinity. This takes 10 seconds at UHV and requires at least tier 1 refinery coils.\n\nBoth dust ingredients are consumed every batch. The word catalyst in an ingredient name is not a promise that you will get it back.',
  '硅岩燃料精炼厂找到了第二份工作。投入 50,000mB UV 燃料、1024 个无尽催化剂粉、1024 个原子分离催化剂粉，以及 17,856mB 熔融无尽。配方以 UHV 电压运行 10 秒，精炼厂至少需要 1 级线圈。\n\n两种粉末每批都会消耗。“催化剂”写在名字里，不代表机器一定会把它还给你。'),
 ('uev', 'UEV', 'UEV Benzene', 'UEV苯', 251500000, '§5§l§n', 576,
  ['quantum_catalyst', 3287],
  'Use a tier 2 or better Quantum Force Transformer recipe setup. Each batch takes 50,000 mB of UHV fuel, 1024 Transcendent Metal Dust, 1024 Hypogen Dust and 17,856 mB of Molten Infinity, and runs for 10 seconds at UEV.\n\nKeep a Benzene Quantum Catalyst in a Bulk Catalyst Housing for each parallel. Unlike the refinery powders, this catalyst is reusable. The benzene recipe has one guaranteed fluid output; it does not inherit the random output split of unrelated QFT recipes.',
  '量子力变换机的配方等级至少要达到 2 级。每批投入 50,000mB UHV 燃料、1024 个超越金属粉、1024 个海珀珍粉，以及 17,856mB 熔融无尽，以 UEV 电压运行 10 秒。\n\n每个并行在大宗催化剂仓里留一个苯量子催化剂。它与精炼厂消耗的粉末不同，可以重复使用。苯配方只有一种必定产出的流体，不会套用其他 QFT 配方的随机分流。'),
 ('uiv', 'UIV', 'Causal-Loop Benzene', '因果闭环苯', 3876000000, '§9§l§n', 672,
  ['fuel_uev'],
  'Raise the QFT recipe tier to at least 3. Combine 50,000 mB of UEV fuel with 2048 Transcendent Metal Dust, 2048 Hypogen Dust and 17,856 mB of Molten Space-Time. The processing time is still 10 seconds, now at UIV. Keep the reusable Benzene Quantum Catalyst installed.\n\n[warn]UIV is the first fuel in this line that is not registered in the native GT gas-turbine fuel map: its heat value exceeds that map\'s integer limit. Producing it does not make a native gas turbine accept it. The resonance generator understands its full heat value, but its survival entry components remain pending.[/warn]',
  '将 QFT 配方等级提升到至少 3 级。每批投入 50,000mB UEV 燃料、2048 个超越金属粉、2048 个海珀珍粉，以及 17,856mB 熔融时空。仍然运行 10 秒，但电压提升为 UIV。可复用的苯量子催化剂继续留在仓内。\n\n[warn]从 UIV 开始，热值超过原生 GT 燃气燃料表的整数上限，因此不再注册进原生燃气轮机。做出来不代表旧轮机就能烧它。共振发电塔能够读取完整热值，但它的生存入口材料配方仍待定。[/warn]'),
 ('umv', 'UMV', 'Eternally Attuned Benzene', '永恒调律苯', 66950000000, '§5§l§n', 768,
  ['fuel_uiv'],
  'Tier 4 QFT processing is required now. One batch uses 50,000 mB of UIV fuel, 144 mB each of Molten Shirabon and Molten Eternity, plus 16 mB of Molten White Dwarf Matter. Ten seconds at UMV gives the same volume of upgraded fuel.\n\nThe additive amounts look small after the previous steps. The infrastructure needed to make them is not. Give these fluids their own filtered inputs and keep the shared catalyst in place.',
  '这次需要 4 级 QFT 配方处理能力。每批使用 50,000mB UIV 燃料、熔融 Shirabon与熔融永恒各 144mB，以及 16mB 熔融白矮星物质，以 UMV 电压运行 10 秒，得到等体积的新燃料。\n\n与前几步相比，添加物数量看起来很小。为了做出它们而建造的工厂可一点都不小。给这些流体安排独立过滤输入，并保留通用的苯量子催化剂。'),
 ('uxv', 'UXV', 'Eigenbenzene Cosmic Fuel', '苯征宇宙燃料', 1296000000000, '§6§l§n', 864,
  ['fuel_umv'],
  'Your tree farm has become a rather elaborate excuse to process stellar matter. In a tier 4 QFT, combine 50,000 mB of UMV fuel with 124 mB of Molten Universium, 124 mB of Molten Magnetohydrodynamically Constrained Star Matter, 1984 mB of Raw Star Matter and 144 mB of Molten Eternity. Supply UXV for 10 seconds and keep the reusable catalyst installed.\n\nThis is the last upgrade currently available. The task checks the fuel itself, not a promised generator output: generation still depends on a working generator, supplied base fluids and the installed upgrades.',
  '你的树场现在成了处理恒星物质的一个相当复杂的借口。在 4 级 QFT 中，将 50,000mB UMV 燃料与 124mB 熔融宇宙素、124mB 熔融磁流体约束恒星物质、1984mB 原始恒星物质，以及 144mB 熔融永恒混合，以 UXV 电压运行 10 秒。仍需保留可复用催化剂。\n\n这就是目前燃料线的终点。任务检测的是实际燃料，不是一笔承诺发给你的电：真正的发电量还取决于可工作的发电机、底数流体供应和已安装的升级。'),
]

add('uv_activator', '§2§l§nJust Add More Neutrons', '§2§l§n再加一点中子',
    'In a Large Chemical Reactor, process 1024 Neutronium Dust, 1024 Cosmic Neutronium Dust and 16,384 mB of Americium Plasma at UV for 10 seconds. You receive 144 mB of UV Activator, exactly enough for one fuel-upgrade batch.\n\nThis intermediate is a fluid. Use a suitable fluid container to detect 144 mB, then send it on to fusion. Do not search for an activator cell that this mod does not register.',
    '在大型化学反应釜中，将 1024 个中子素粉、1024 个宇宙中子素粉与 16,384mB 镅等离子体，以 UV 电压加工 10 秒。产出的 144mB UV 活化液，正好够下一步升级一批燃料。\n\n这个中间产物是流体。用合适的流体容器在背包里检测 144mB，再送去聚变；本模组并没有单独注册一种活化液单元。',
    384, 72, component(3), ['fuel_zpm'], [fluid('benzenefuels.uv_activator', 144)], [coin('Chemist', 1)])

add('quantum_catalyst', '§5§l§nThis One Comes Back', '§5§l§n这次真的不消耗',
    'An UEV Assembler combines 8 dust each of Platinum, Palladium and Iridium with one UEV Field Generator and 144 mB of Molten Infinity. After 10 seconds you have a Benzene Quantum Catalyst.\n\nPut it in the QFT Bulk Catalyst Housing, not into a normal input bus. You need one per parallel, but you do not need to remake it for every batch. UEV through UXV all use this same catalyst.',
    '用 UEV 组装机将铂粉、钯粉、铱粉各 8 个，一个 UEV 力场发生器，以及 144mB 熔融无尽加工 10 秒，得到苯量子催化剂。\n\n把它放进 QFT 的大宗催化剂仓，不是普通输入总线。每个并行需要一个，但不必每批重做。UEV 到 UXV 的四级燃料共用这一种催化剂。',
    576, 72, component(30000), ['fuel_uhv', 3287], [retrieval([component(30000)])], [coin('Chemist', 2)])

for i, (key, tier, en, zh, heat, color, x, parents, en_text, zh_text) in enumerate(FUELS):
    en_text += f'\n\nFuel value: {heat:,} EU/mB ({heat*1000:,} EU per 1000 mB), before generator efficiency. Bring 16,000 mB in fluid containers for detection; nothing is consumed.'
    zh_text += f'\n\n基础热值：{heat:,}EU/mB，即每 1000mB 为 {heat*1000:,}EU，尚未计算发电机效率。携带装有合计 16,000mB 燃料的容器进行检测，不会消耗燃料。'
    add('fuel_'+key, color+tier+' — '+en, color+tier+' — '+zh, en_text, zh_text,
        x, 144, component(i+1), parents, [fluid('benzenefuels.'+key, 16000)],
        [coin('Chemist', 2 if i<3 else 3 if i<7 else 8)], main=True)

add('gas_generators', 'Read the Fine Print', '先看清楚油箱盖',
    'LuV through UEV Benzene are registered as native GT gas-turbine fuels. That makes them fuel candidates, not a guarantee that every rotor or dynamo is suitable. Heat value, turbine efficiency, optimal flow and output capacity are different numbers.\n\nUIV, UMV and UXV are not on that native fuel map. Nor does a gas registration automatically make any fuel a rocket-engine fuel. Check the actual recipe category in NEI before designing your power plant.\n\n[note]A fuel upgrade keeps the fluid volume, not the processing cost. Include the electricity and additives used along the way when comparing a complete power line.[/note]',
    'LuV 至 UEV 的苯已注册为原生 GT 燃气轮机燃料。这说明它们能进入对应燃料表，不代表任意转子与动力仓都能合适地处理它们。热值、涡轮效率、最优流量与输出容量，是四件不同的事。\n\nUIV、UMV、UXV 不在原生燃气燃料表里。注册成燃气也不会自动让火箭引擎接受它。规划电站前，先在 NEI 中确认实际的燃料分类。\n\n[note]升级保留的是流体体积，不是加工成本。比较整条产线时，要把一路用掉的电和添加物也算进去。[/note]',
    192, 228, stack('gregtech:gt.blockmachines', 36), ['fuel_luv'])

add('automation', 'One Pipe, One Job', '一根管子，一份工作',
    'Keep a buffer between each fuel tier and reserve the lower-tier fuel you still burn. These recipes upgrade the entire 50,000 mB batch; a shared export bus with no limits can starve the old power plant while the new one is still being built.\n\nFor QFT, keep the reusable catalyst in its housing, request the consumable additives separately and filter the output back into its own tank. The eight fuel tasks are one-time milestones. They are not repeatable rewards for circulating the same fuel.',
    '各级燃料之间留缓冲，也给仍在使用的低级发电机预留口粮。每次升级处理的是完整的 50,000mB；一个没有限制的共用输出总线，可能在新电站还没建好时，就先把旧电站饿停。\n\nQFT 的可复用催化剂留在仓里，消耗性添加物单独请求，产物过滤回各自的储罐。八级燃料任务都是一次性里程碑，不是靠来回搬运同一桶燃料反复刷奖励的生意。',
    672, 228, component(30000), ['fuel_uev'])

PENDING_EN = '\n\n[note]This is a reference branch. Survival recipes for base fluids, exponent cores, the controller and its light casing are not yet defined. Read and check the box; no pending material is requested or awarded, and this branch does not gate the fuel chain.[/note]'
PENDING_ZH = '\n\n[note]这是说明支线。底数流体、指数核心、主机和导光外壳的生存合成配方尚未确定。阅读后勾选即可，不要求提交，也不会奖励待定材料；这条支线不阻挡燃料主线。[/note]'

GUIDES = [
 ('tower', '§2§l§nA Power Plant with Altitude', '§2§l§n电站也要有高度', 288, 336, stack('gregtech:gt.blockmachines',32240), ['fuel_uv'],
  'Plan this project around UV progression. The complete tower is 33×33×129, so find a site before ordering the blocks. Its controller sits two blocks above the bottom; controller Y=2 through 129 keeps the whole structure within the normal world height.\n\nYou will need fusion infrastructure for fusion casings, quantum glass, a large collection of industrial casings and 1648 matching heating coils. UV is the intended entry stage, not a claim that the currently missing controller recipe can be crafted.',
  '按 UV 阶段规划这项工程。整座塔尺寸为 33×33×129，先找地，再下材料订单。主机位于底部以上两格；主机放在 Y=2～129，才能让整座结构落在普通世界的高度范围内。\n\n你需要用于聚变机械方块的聚变基础设施、量子玻璃、大量工业机械方块，以及 1648 个同级加热线圈。UV 是计划中的入口阶段，不表示当前尚缺配方的主机已经能在生存模式合成。'),
 ('tower_materials', 'Order the Casings First', '先让工厂做外壳', 384, 336, stack('gregtech:gt.blockcasings4',6), ['tower'],
  'The default structure contains 15,472 blocks. The largest orders are 3628 Quantum Glass, 1940 Clean Stainless Steel Machine Casings, 1648 heating coils, 1423 Fusion Machine Casings and 1215 Solid Steel Machine Casings. It also uses 621 Blue-White Resonance Light Casings.\n\nOther materials include frost-proof, grating, filter, chemically inert, magic and field-containing casings, cooling and PTFE pipes, superconducting coils and small decorative parts. Use the structure preview for the full arrangement. The list below is an optional planning checklist, not a demand to put an entire tower into your inventory.',
  '默认结构一共使用 15,472 个方块。最大的几笔订单是 3628 个量子玻璃、1940 个洁净不锈钢机械方块、1648 个加热线圈、1423 个聚变机械方块，以及 1215 个坚固钢机械方块。此外还有 621 个蓝白共振导光外壳。\n\n其余用料包括防冻、格栅、过滤、化学惰性、魔法与遏制场机械方块，冷却和聚四氟乙烯管道，超导线圈，以及少量装饰件。完整位置请看结构预览。下面的清单只是可选的备料提示，不要求把整座塔塞进背包。'),
 ('tower_coils', 'More Coils, More Batches', '线圈决定胃口', 480, 336, stack('gregtech:gt.blockcasings5',0), ['tower_materials'],
  'All 1648 coils must be the same tier. Cupronickel gives one parallel, Kanthal four, and Eternity 67,108,864. The limit is floor(4^((heat−901)/900−1)), using real division. A mixed jacket will not form.\n\nEach parallel burns 1000 mB once per five-second batch. Actual parallel is the smallest of the configured limit, the coil limit and the whole buckets of fuel available. Better coils are a ceiling, not a command to conjure more fuel.',
  '全部 1648 个线圈必须同级。白铜支持 1 并行，坎塔尔支持 4，并行上限到永恒线圈为 67,108,864。计算式为 floor(4^((热量−901)/900−1))，除法按实数计算。混用线圈不能成型。\n\n每个并行每五秒消耗 1000mB 燃料。实际并行取设置上限、线圈上限和可用燃料整桶数中的最小值。更好的线圈提高的是上限，可不会凭空变出燃料。'),
 ('tower_hatches', 'Front Door, Back Door', '前门进料，后门送电', 576, 336, stack('gregtech:gt.blockmachines',56), ['tower_coils'],
  'Install one to three fluid input hatches and exactly one Maintenance Hatch in the permitted chemically inert casings on the controller face, at model Y=2 or 3. The lowest row is not a hatch row.\n\nThe two rear Solid Steel Machine Casing rows accept up to sixteen dynamo hatches. Wireless operation can use no dynamos. In the structure preview, right-click a permitted casing to see replacements. You still need to supply both base fluids as well as the fuel.',
  '在主机同一面、模型 Y=2 或 3 的指定化学惰性机械方块位置，安装 1～3 个流体输入仓，并且恰好安装 1 个维护仓。最下面一排不是仓室位。\n\n背面两排指定的坚固钢机械方块允许安装最多 16 个动力仓。无线模式可以不装动力仓。在结构预览中右键允许替换的外壳，即可查看候选方块。除了燃料，两种底数流体仍然都要供应。'),
 ('tower_operation', 'Five Seconds at a Time', '每五秒，吃一顿', 672, 336, component(3), ['tower_hatches'],
  'At the start of a batch, the tower checks only the first non-base fluid it reads. An invalid first fuel prevents startup; it does not search later tanks for something tastier. It consumes whole 1000 mB units up to its effective parallel limit, plus 1 mB of each base fluid per unit.\n\nBoth base fluids must cover the entire planned batch. With the default exponents of 1, fuel efficiency is technology base × magic base. The energy is then spread evenly across 100 ticks. Fuel is prepaid, so dismantling the machine mid-batch does not return it.',
  '每批开始时，主机只检查读取到的第一种非底数流体。如果它不是有效燃料，机器不会继续搜索后面的罐子。燃料按 1000mB 的整数倍消耗，并受有效并行上限限制；每份同时需要科技和魔法底数流体各 1mB。\n\n两种底数流体都必须够支付整批。初始两个指数均为 1，此时燃料效率就是科技底数×魔法底数。能量在接下来的 100 tick 内均匀输出。燃料已经预付，运行中拆机不会退款。'),
 ('tower_output', 'Do Not Confuse Storage with Throughput', '容量不是吞吐量', 768, 336, stack('gregtech:gt.blockmachines',15228), ['tower_operation'],
  'Wired mode inserts energy into dynamos within each hatch\'s voltage × amperage limit and free buffer space. The controller\'s delivered value measures this insertion, not energy already used by a distant machine. Hover over the statistics to inspect the real buffer. A disconnected hatch fills up, and the remaining generation is lost.\n\nWireless mode sends energy to the owner\'s wireless account. Mode changes take effect with the next batch. Neither mode pauses the fuel recipe to wait for somewhere to store excess power.',
  '有线模式按每个动力仓的电压×安培数，以及剩余缓存空间注入电能。主机显示的“实际发送”是这笔注入量，不是远处机器已经用掉的电。悬停主机信息即可检查实际缓存。没有连接设备的动力仓会逐渐充满，后续多余发电就浪费了。\n\n无线模式把能量发送到主机所有者的无线账户。模式修改从下一批生效。无论哪种模式，都不会为了等待存储空间而暂停燃料配方。')]

for key,en,zh,x,y,icon,parents,et,zt in GUIDES:
    tasks = None
    if key == 'tower_materials':
        tasks = [{'taskID:8':'bq_standard:checkbox'}, retrieval([
            stack('tectech:tile.quantumGlass',0,3628), stack('gregtech:gt.blockcasings4',1,1940),
            stack('gregtech:gt.blockcasings4',6,1423), stack('gregtech:gt.blockcasings2',0,1215)], True)]
    add(key,en,zh,et+(PENDING_EN if key=='tower' else ''),zt+(PENDING_ZH if key=='tower' else ''),
        x,y,icon,parents,tasks)

for route,zh_route,y,bases,exps,color,start_meta in [
    ('technology','科技',456,[1.01,1.02,1.05,1.07,1.1],[2,4,8,16,32],'§b',200),
    ('magic','魔法',552,[1.01,1.05,1.1,1.2,1.3],[2,4,6,8,10],'§d',210)]:
    tech=route=='technology'
    add(route+'_route', color+('Small Base, Big Exponent' if tech else 'A Little More Magic'),
        color+('小底数，大指数' if tech else '大底数，小指数'),
        ('The technology route trades modest base multipliers for a large permanent exponent. ' if tech else 'The magic route offers larger base multipliers with a smaller permanent exponent. ')+
        'Base fluids are consumed; exponent cores are installed once. The two routes multiply together, and neither replaces the other.\n\nThe controller begins at exponent 1. Feed the next core tier into its lower-right slot to permanently raise that route. You cannot skip tiers, install a duplicate for another bonus, or use a Voltage Star in place of a core.'+PENDING_EN,
        ('科技侧用较小的底数，换取较高的永久指数。' if tech else '魔法侧的底数更大，但永久指数相对较低。')+
        '底数流体持续消耗，指数核心只需安装一次。两侧倍率相乘，不能拿一侧替代另一侧。\n\n主机初始指数为 1。把下一级核心放入右下角物品格，该侧就会永久升级。不能跳级，重复安装不会叠加，也不能拿电压之星代替指数核心。'+PENDING_ZH,
        288,y,component(start_meta),['tower'])
    for i,(base,exp) in enumerate(zip(bases,exps)):
        mk=i+1;tier=['LuV','UV','UEV','UMV','UXV'][i];prev=route+'_route' if not i else f'{route}_mk{i}'
        multiplier=f'{base**exp:.4f}'
        add(f'{route}_mk{mk}',color+f'{route.title()} Mk{mk}',color+f'{zh_route}路线 Mk{mk}',
            f'Mk{mk} {route} base fluid supplies a base of {base:g}; the Mk{mk} core raises this route\'s exponent to {exp}. Together they contribute about {multiplier}×, before the other route is multiplied in. '+
            (f'The technology fluid is planned for {tier} progression.' if tech else 'The magic fluid progression has no final recipe or independent unlock stage yet.')+
            '\n\nThe consumption remains 1 mB per 1000 mB of fuel, regardless of Mk. Bring this core only after installing the previous tiers; swapping base fluids does not install a core.\n\n[note]Reference only: no survival recipe, item submission or material reward is defined for this node.[/note]',
            f'Mk{mk} {zh_route}底数流体提供 {base:g} 的底数，Mk{mk} 核心将该侧指数升级至 {exp}。这组搭配单独提供约 {multiplier} 倍，之后再乘上另一侧的倍率。'+
            (f'科技底数流体计划在 {tier} 阶段解锁。' if tech else '魔法流体的具体配方与独立解锁阶段尚未确定。')+
            '\n\n无论 Mk 等级如何，每 1000mB 燃料都只消耗该侧底数 1mB。先装完前面的核心等级，再安装这一级；更换底数流体并不等于安装了核心。\n\n[note]本节点仅供阅读：暂无生存配方，不要求提交，也不发放材料奖励。[/note]',
            384+i*96,y,component(start_meta+i),[prev])

add('stars', 'Stars Made in an Assembler', '组装机里，也有星星',
    'Voltage Stars package the eight basic component families of a voltage tier: Motor, Piston, Pump, Conveyor Module, Robot Arm, Emitter, Sensor and Field Generator. Start with one of each LV component in an LV Assembler for 8 seconds.\n\nThis is a separate collection and manufacturing branch. Stars are not currently ingredients of fuel, base-fluid or exponent-core recipes. In particular, a star does not upgrade the generator. The tasks below detect one star without consuming it, so it can go straight into the next recipe.',
    '电压之星打包了同电压等级的八类基础部件：马达、活塞、泵、传送带、机械臂、发射器、传感器和力场发生器。起点是在 LV 组装机中，以 LV 电压把各一个 LV 部件加工 8 秒。\n\n这是一条独立的收集与制造支线。目前星星不是燃料、底数流体或指数核心的配方材料，尤其不能拿来直接升级发电机。下方任务只检测一个星星，不会收走它，可以继续投入下一级配方。',
    0,696,component(100),['welcome'])
for i,tier in enumerate(['LV','MV','HV','EV','IV','LuV','ZPM','UV','UHV','UEV','UIV','UMV','UXV']):
    count=min(2**i,1024);previous=0 if i==0 else 16 if i<=5 else 4;seconds=8*2**i
    prev=['stars'] if i==0 else ['star_'+str(i-1)]
    machine='Assembly Line' if i>=5 else 'Assembler';zh_machine='装配线' if i>=5 else '组装机'
    en=f'Use {count} of each of the eight {tier} components'+(f' and {previous} stars of the previous voltage tier' if previous else '')+f' in the {machine}. The recipe uses {tier} voltage for {seconds:,} seconds and makes one {tier} Star.'
    zh=f'将八类 {tier} 基础部件各 {count} 个'+(f'，以及 {previous} 个上一级电压之星' if previous else '')+f'送入{zh_machine}，以 {tier} 电压加工 {seconds:,} 秒，得到一个 {tier} 之星。'
    if i>=5:
        en+='\n\nScan the previous star at this tier for 60 seconds to obtain the Assembly Line research. The star recipe has no additional fluid input.'
        zh+='\n\n以本级电压扫描上一级之星 60 秒，取得装配线研究数据。星星的加工配方没有额外的流体输入。'
    en+='\n\nThe task only detects the star. It does not consume it, grant another star or unlock a pending generator upgrade.'
    zh+='\n\n任务只检测星星，不会收走它、额外赠送星星，或解锁尚未确定的发电机升级配方。'
    add('star_'+str(i),f'{tier} Star',f'{tier}之星',en,zh,96+i*72,696,component(100+i),prev,
        [retrieval([component(100+i)])],[coin('Technician',1,'I' if i<2 else 'II' if i<5 else 'III')])


def validate():
    """Reject broken references, cycles, overlapping nodes and unavailable-material gates."""
    by_id = {uid(row['key']): row for row in QUESTS}
    assert len(by_id) == len(QUESTS) == 46
    native = {uuid.UUID(int=value) for value, _, _ in ANCHORS}
    def decode(value):
        return uuid.UUID(int=((value['questIDHigh:4'] & ((1<<64)-1))<<64)
                         | (value['questIDLow:4'] & ((1<<64)-1)))
    visited, visiting = set(native), set()
    def visit(qid):
        if qid in visited:
            return
        assert qid not in visiting, 'Cycle: '+str(qid)
        assert qid in by_id, 'Missing prerequisite: '+str(qid)
        visiting.add(qid)
        for parent in by_id[qid]['quest']['preRequisites:9'].values():
            visit(decode(parent))
        visiting.remove(qid); visited.add(qid)
    for qid in by_id:
        visit(qid)
    entries=[row['entry'] for row in QUESTS]
    entries += [{'x:3':x,'y:3':y,'sizeX:3':24,'sizeY:3':24} for _,x,y in ANCHORS]
    for i,a in enumerate(entries):
        for b in entries[i+1:]:
            assert (a['x:3']+a['sizeX:3']<=b['x:3'] or b['x:3']+b['sizeX:3']<=a['x:3']
                    or a['y:3']+a['sizeY:3']<=b['y:3'] or b['y:3']+b['sizeY:3']<=a['y:3']), 'Overlapping nodes'
    for row in QUESTS:
        quest=row['quest'];tasks=quest['tasks:9']
        pending=row['key'].startswith(('tower','technology','magic'))
        if pending:
            assert tasks['0:10']['taskID:8']=='bq_standard:checkbox'
            assert not quest['rewards:9']
        assert quest['properties:10']['betterquesting:10']['repeatTime:3']==-1
        for task in tasks.values():
            assert task.get('consume:1',0)==0 and task.get('autoConsume:1',0)==0
    assert set(TEXT['zh_CN'])==set(TEXT['en_US'])


def main():
    validate()
    chapter_id=uid('chapter')
    chapter={**identity(chapter_id,'questLineID'), 'properties:10':{'betterquesting:10':{
        'name:8':'Benzene Beyond the Horizon','desc:8':'Eight fuel upgrades, one very tall generator, and two routes to higher efficiency.',
        'icon:10':component(1),'visibility:8':'ALWAYS','bg_image:8':'','bg_size:3':256}}}
    for lang,name,desc in [('en_US','Benzene Beyond the Horizon','Eight fuel upgrades, one very tall generator, and two routes to higher efficiency.'),
                          ('zh_CN','苯向星辰','八级燃料进阶，一座高耸的发电塔，以及科技与魔法两条效率路线。')]:
        prefix='betterquesting.questline.'+encoded(chapter_id).rstrip('=')
        TEXT[lang][prefix+'.name']=name;TEXT[lang][prefix+'.desc']=desc
    # Native anchors are references to existing quests, never cloned definitions.
    anchors=[{**identity(uuid.UUID(int=q)), 'x:3':x,'y:3':y,'sizeX:3':24,'sizeY:3':24} for q,x,y in ANCHORS]
    bundle={'schema':1,'chapter':chapter,'quests':QUESTS,'anchors':anchors,
            'insertAfter':str(uuid.UUID(int=21)), 'namespace':str(NS)}
    out=ROOT/'src/main/resources/assets/benzenefuels/quests/chapter.json'
    out.write_text(json.dumps(bundle,ensure_ascii=False,indent=2)+'\n')
    for lang,entries in TEXT.items():
        path=ROOT/f'src/main/resources/assets/benzenefuels/lang/{lang}.lang'
        start='# BEGIN GENERATED BENZENE QUESTS';end='# END GENERATED BENZENE QUESTS'
        current=path.read_text();current=current.split(start)[0].rstrip()+'\n\n'
        current+=start+'\n'+''.join(k+'='+v.replace('%','%%').replace('\r','').replace('\n','%n')+'\n' for k,v in entries.items())+end+'\n'
        path.write_text(current)
    print(f'{len(QUESTS)} authored quests, {len(ANCHORS)} native anchors; chapter {chapter_id}')


if __name__=='__main__':
    main()
