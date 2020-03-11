#!/bin/bash
# fail if any commands fails
set -e

export FIREBASE_GROUP="internal-beta"

./gradlew assembleDebug appDistributionUploadDebug
./gradlew assembleMainnet appDistributionUploadMainnet
