#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

LAST_SRC_HASH_FILE=".last_sources_hash"
LAST_APK_HASH_FILE=".last_apk_hash"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Hash tracked and untracked source files
src_hash=$(git ls-files -c -o --exclude-standard 2>/dev/null \
  | while IFS= read -r file; do
      [ -f "$file" ] && sha1sum "$file"
    done \
  | sort \
  | sha1sum \
  | awk '{print $1}')

prev_src_hash=""
prev_apk_hash=""
[ -f "$LAST_SRC_HASH_FILE" ] && prev_src_hash="$(cat "$LAST_SRC_HASH_FILE")"
[ -f "$LAST_APK_HASH_FILE" ] && prev_apk_hash="$(cat "$LAST_APK_HASH_FILE")"

if [ "$src_hash" != "$prev_src_hash" ]; then
  echo "Sources changed -> running ./gradlew assembleDebug"
  ./gradlew assembleDebug
  echo "$src_hash" > "$LAST_SRC_HASH_FILE"
else
  echo "No source changes detected — skipping assembleDebug"
fi

if [ -f "$APK_PATH" ]; then
  apk_hash=$(sha1sum "$APK_PATH" | awk '{print $1}')
  if [ "$apk_hash" != "$prev_apk_hash" ]; then
    echo "APK changed -> running ./gradlew installDebug"
    ./gradlew installDebug
    echo "$apk_hash" > "$LAST_APK_HASH_FILE"
  else
    echo "APK unchanged — skipping installDebug"
  fi
else
  echo "APK not found at $APK_PATH — nothing to install"
fi
