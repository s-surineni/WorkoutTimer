#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# Find the first connected Android emulator
EMULATOR_SERIAL=$(adb devices | grep -E '^emulator-[0-9]+\s+device' | head -n 1 | awk '{print $1}')

if [ -z "${EMULATOR_SERIAL:-}" ]; then
  echo "❌ Error: No running Android emulator found. Please start an emulator (e.g. via Android Studio AVD)."
  exit 1
fi

echo "📱 Targeting Emulator: $EMULATOR_SERIAL"

echo "🔨 Building Debug and AndroidTest APKs..."
./gradlew assembleDebug assembleDebugAndroidTest --quiet

echo "📦 Installing APKs on $EMULATOR_SERIAL..."
adb -s "$EMULATOR_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk > /dev/null
adb -s "$EMULATOR_SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk > /dev/null

echo "🧪 Running Instrumented Tests on $EMULATOR_SERIAL..."
adb -s "$EMULATOR_SERIAL" shell am instrument -w -r com.example.workouttimer.test/androidx.test.runner.AndroidJUnitRunner

