#!/usr/bin/env python3
"""Validate in a new disposable dedicated GTNH server; never start a game client."""
import argparse
import json
import shutil
import subprocess
import time
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pack", type=Path, required=True)
    parser.add_argument("--java", type=Path, required=True)
    parser.add_argument("--fixture", type=Path, required=True)
    parser.add_argument("--run-dir", type=Path, required=True)
    args = parser.parse_args()
    source, target = args.pack.resolve(), args.run_dir.resolve()
    if target.exists() or source == target or source in target.parents:
        parser.error("Use a new disposable directory outside the source server")
    if not (source / "mods").is_dir() or not args.fixture.is_file():
        parser.error("Missing source server or compiled test fixture")
    target.mkdir(parents=True)
    (target / "mods").mkdir()
    for mod in (source / "mods").iterdir():
        if any(name in mod.name.lower() for name in (
            "benzenefuels", "plasmaforgepower", "structureterminal", "aeorderoptimizer", "aeoptimizer", "aeorderbench"
        )):
            continue
        (target / "mods" / mod.name).symlink_to(mod, target_is_directory=mod.is_dir())
    for name in ("config", "scripts", "serverutilities", "blueprints"):
        if (source / name).is_dir():
            shutil.copytree(source / name, target / name)
    for name in ("libraries", "nestedmods"):
        if (source / name).is_dir():
            (target / name).symlink_to(source / name, target_is_directory=True)
    for file in source.iterdir():
        if file.is_file() and (file.suffix == ".jar" or file.name == "java9args.txt"):
            shutil.copy2(file, target / file.name)
    shutil.copy2(args.fixture, target / "mods/benzenefuels-tests.jar")
    (target / "eula.txt").write_text("eula=true\n")
    (target / "server.properties").write_text(
        "server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\n"
        "level-name=benzene-validation\nlevel-type=FLAT\ngenerator-settings=2;7,2x3,2;1;\n"
        "generate-structures=false\nspawn-protection=0\nview-distance=2\nallow-nether=false\n"
        "spawn-monsters=false\nspawn-animals=false\nmax-tick-time=120000\n"
    )
    command = [str(args.java.resolve()), "-Xms1G", "-Xmx6G", "-XX:+UseG1GC",
               "-Dbenzenefuels.tests=true", "-Djava.awt.headless=true", "-Duser.language=en",
               "@java9args.txt", "-jar", "lwjgl3ify-forgePatches.jar", "nogui"]
    with (target / "test-server.log").open("w") as output:
        process = subprocess.Popen(command, cwd=target, stdout=output, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 1200
            while process.poll() is None:
                time.sleep(5)
                if time.monotonic() > deadline:
                    raise TimeoutError("Server validation exceeded 20 minutes")
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=30)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
    path = target / "benzene-fuels-tests.json"
    if not path.exists():
        raise SystemExit("Missing report. See " + str(target / "test-server.log"))
    report = json.loads(path.read_text())
    print(json.dumps({"exit": process.returncode, "passed": report.get("passed"),
                      "checks": len(report.get("tests", [])), "report": str(path)}, ensure_ascii=False))
    if process.returncode or not report.get("passed"):
        raise SystemExit(1)


if __name__ == "__main__":
    main()
