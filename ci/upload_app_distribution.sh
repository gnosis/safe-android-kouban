#!/bin/bash
# fail if any commands fails
set -e

export FIREBASE_GROUP="internal-beta"

echo "INFURA_API_KEY=$INFURA_API_KEY" > project_keys

./gradlew assembleDebug appDistributionUploadDebug
