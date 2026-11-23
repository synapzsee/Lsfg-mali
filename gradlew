#!/bin/sh
# Gradle wrapper script
export GRADLE_HOME="${GRADLE_HOME:-${HOME}/.gradle/wrapper/dists/gradle-8.14.4-all}"
if [ ! -f "${GRADLE_HOME}/bin/gradle" ]; then
  mkdir -p "${GRADLE_HOME}"
  cd "${GRADLE_HOME}"
  curl -L -o gradle.zip "https://services.gradle.org/distributions/gradle-8.14.4-all.zip"
  unzip -q gradle.zip
  mv gradle-8.14.4/* .
  rm -rf gradle-8.14.4 gradle.zip
fi
exec "${GRADLE_HOME}/bin/gradle" "$@"
