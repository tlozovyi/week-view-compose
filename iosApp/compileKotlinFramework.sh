#!/bin/sh
set -e

if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
  exit 0
fi

if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
  if [ -z "$JAVA_HOME" ]; then
    echo "error: JDK 17 is required to build the Kotlin framework. Install JDK 17 and retry." >&2
    exit 1
  fi
  export JAVA_HOME
fi

export PATH="$JAVA_HOME/bin:$PATH"

cd "$SRCROOT/.."
./gradlew :sample:embedAndSignAppleFrameworkForXcode
