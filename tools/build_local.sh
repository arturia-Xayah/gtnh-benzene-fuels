#!/usr/bin/env bash
set -euo pipefail
benzene_root="$(cd "$(dirname "$0")/.." && pwd)"
benzene_workspace="$(cd "$benzene_root/../.." && pwd)"
benzene_cache="$benzene_workspace/work/terminal_port_sources"
cd "$benzene_root"

# Reuse an existing authoring workspace when present; a normal clone uses gradlew.
benzene_java25="${BENZENE_JAVA25:-$benzene_cache/jdk/zulu25.36.205-ca-jdk25.0.4.1-macosx_aarch64/Contents/Home}"
benzene_java8="${BENZENE_JAVA8:-$benzene_cache/jdk8/zulu8.96.0.205-ca-jdk8.0.504-macosx_aarch64/Contents/Home}"
benzene_java17="${BENZENE_JAVA17:-$benzene_workspace/outputs/structure_terminal/.tools/jdk17/zulu17.68.203-ca-jdk17.0.20.1-macosx_aarch64/Contents/Home}"
benzene_gradle="${BENZENE_GRADLE:-$benzene_root/gradlew}"
if [ -x "$benzene_java25/bin/java" ]; then export JAVA_HOME="$benzene_java25"; fi
if [ -d "$benzene_cache/gradle-cache" ]; then
    export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$benzene_cache/gradle-cache}"
fi
if [ -z "${BENZENE_GRADLE:-}" ] && [ -x "$benzene_cache/gradle/gradle-9.7.1/bin/gradle" ]; then
    benzene_gradle="$benzene_cache/gradle/gradle-9.7.1/bin/gradle"
fi
benzene_options=()
if [ -x "$benzene_java25/bin/java" ] && [ -x "$benzene_java8/bin/java" ] && [ -x "$benzene_java17/bin/java" ]; then
    benzene_options+=("-Porg.gradle.java.installations.paths=$benzene_java25,$benzene_java8,$benzene_java17")
    benzene_options+=(-Porg.gradle.java.installations.auto-download=false)
fi
if [ "$#" -eq 0 ]; then set -- build; fi
exec "$benzene_gradle" "$@" --no-daemon --no-configuration-cache --console=plain "${benzene_options[@]}"
