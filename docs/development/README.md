# 开发者文档 / Developer documentation

[中文首页](../../README.md) · [English README](../../README.en.md)

## 构建

安装 JDK **25、17、8**，将 `JAVA_HOME` 指向 JDK 25 后执行：

```sh
chmod +x gradlew
./gradlew build --no-daemon --console=plain
```

Gradle 使用 25，工具链可读取 17 / 8，运行时代码编译为 Java 8 字节码。`build` 包含格式、Checkstyle、单元测试及发行检查，不启动 Minecraft。GitHub Actions 使用同样的三套 JDK。

玩家安装包为 `build/libs/benzenefuels-0.5.1-alpha.jar`。发布时额外加上 GTNH 版本，形成 `benzenefuels-0.5.1-alpha-GTNH-2.9.0.jar`。`-dev`、`-sources` 和测试夹具不用于玩家安装。

`tools/build_local.sh` 可复用本地工作区缓存，也支持 `BENZENE_JAVA25`、`BENZENE_JAVA8`、`BENZENE_JAVA17` 与 `BENZENE_GRADLE` 指定工具路径；标准构建不依赖原作者目录。

## 后台服务端验证

```sh
./gradlew build -PbenzeneFuelsTests=true
cp build/libs/benzenefuels-0.5.1-alpha.jar /tmp/benzenefuels-tests.jar
python3 tools/run_backend.py \
  --pack /path/to/official-GTNH-2.9.0-server \
  --java /path/to/java25/bin/java \
  --fixture /tmp/benzenefuels-tests.jar \
  --run-dir /new/disposable/benzene-validation
./gradlew build
```

仅显式参数把 `src/packTest` 加入构建。运行器创建独立世界，绑定本机随机端口，使用 `nogui` 与 headless 参数；不复用玩家世界或启动客户端。测试完成后重新构建生产包，避免误发夹具。

## 源码入口

| 内容 | 入口 |
|---|---|
| 注册、主机 ID 与配置 | `BenzeneFuels` |
| 流体、单元、图标与材料 | `FuelContent`、`FuelStage` |
| 燃料升级、电压之星 | `FuelRecipes`、`StarRecipes`、`VoltageStar` |
| 结构与仓室区域 | `GeneratorStructure`、运行时结构 JSON |
| 100 tick 发电、存档、无线记账 | `ResonanceGenerator`、`GeneratorMath` |
| 主机 GUI、格式与物品说明 | `ResonanceGeneratorGui`、`GeneratorFormat`、`FuelDescription` |
| 原生任务章节、增量安装 | `quest/BenzeneQuestChapter`、`questbook/build_chapter.py` |

任务书作者工具使用 Python 3 标准库。`python3 questbook/build_chapter.py` 生成原生任务和双语文本；`python3 questbook/build_preview.py` 生成可点击布局预览。细节见[任务章节设计](../features/QUESTS.md)。

生产源码位于 `src/main/java/dev/benzenefuels`。编译和测试依赖固定为 GTNH 2.9.0 正式版对应版本。

## 美术与结构

- [燃料原画、提示词和动画资源](../../design/fuel-textures/README.md)：作者工具使用 Pillow / NumPy，玩家无需 Python。执行 `python3 tools/build_fuel_textures.py` 可从已保存原画再生成。
- [结构材料清单](../../design/generator-materials.md)及[CSV](../../design/generator-bom.csv)。
- [原生材质来源](../../preview/TEXTURE-SOURCES.md)及[鸣谢](../../THIRD_PARTY_NOTICES.md)。历史结构材质提取脚本需要其指定版本的 GT JAR；正常构建直接使用已经保存的资源。
- [验证结果与边界](VALIDATION.md)；玩家安装包在 GitHub Release 中提供，不提交到源码树。

## English

Install Java 25, 17 and 8, set `JAVA_HOME` to Java 25, then run `./gradlew build`. Runtime classes target Java 8; build/check do not launch Minecraft. GitHub Actions uses the same toolchains. The standard build does not depend on the author's workspace.

Opt into `-PbenzeneFuelsTests=true` only for the dedicated-server fixture. Copy that JAR aside, run `tools/run_backend.py` against an official server pack and a new disposable directory, then rebuild production without the flag. It never uses player worlds or starts a game client. Artwork regeneration needs Pillow/NumPy only for authors; game runtime has no new animation dependency.
