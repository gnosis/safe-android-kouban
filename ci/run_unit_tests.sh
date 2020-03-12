#!/bin/bash
# fail if any commands fails
set -e

./gradlew clean assembleMainnet
./gradlew clean assembleDebug testDebugUnitTest --stacktrace
