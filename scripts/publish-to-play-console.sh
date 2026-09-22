#!/usr/bin/env bash
# ==============================================================================
# WorkoutTimer - Google Play Console Build & Publish Script
# ==============================================================================
# This script runs unit tests, compiles the signed release Android App Bundle
# (.aab) and APK, and publishes the bundle to Google Play Console via Gradle
# Play Publisher (GPP) if service account credentials are provided.
# ==============================================================================

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

BUNDLE_ONLY=false
SKIP_TESTS=false
TRACK="internal"

show_help() {
  cat << 'HELP'
Usage: ./scripts/publish-to-play-console.sh [OPTIONS]

Options:
  --bundle-only       Build signed AAB and APK without attempting to upload to Play Console.
  --track <track>     Specify Play Console track: internal (default), alpha, beta, production.
  --skip-tests        Skip running JVM unit tests before building.
  -h, --help          Show this help message and exit.

Examples:
  ./scripts/publish-to-play-console.sh                  # Test, build, and publish to internal track
  ./scripts/publish-to-play-console.sh --bundle-only    # Build signed bundle & APK only
  ./scripts/publish-to-play-console.sh --track alpha   # Publish to alpha track
HELP
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --bundle-only)
      BUNDLE_ONLY=true
      shift
      ;;
    --track)
      TRACK="$2"
      shift 2
      ;;
    --skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    -h|--help)
      show_help
      exit 0
      ;;
    *)
      echo "❌ Unknown option: $1"
      show_help
      exit 1
      ;;
  esac
done

echo "================================================================="
echo "🏋️  WorkoutTimer: Google Play Console Release Workflow"
echo "================================================================="

# 1. Verify signing configuration
KEYSTORE_FILE=""
if [ -f "keystore.properties" ]; then
  echo "🔑 Keystore configuration: keystore.properties found."
  STORE_FILE_PROP=$(grep "^storeFile=" keystore.properties | cut -d'=' -f2- | tr -d '[:space:]')
  if [ -n "$STORE_FILE_PROP" ]; then
    if [[ "$STORE_FILE_PROP" = /* ]]; then
      KEYSTORE_FILE="$STORE_FILE_PROP"
    else
      KEYSTORE_FILE="$ROOT/$STORE_FILE_PROP"
    fi
  fi
fi

if [ -z "$KEYSTORE_FILE" ] || [ ! -f "$KEYSTORE_FILE" ]; then
  if [ -f "workouttimer-upload-key.jks" ]; then
    KEYSTORE_FILE="$ROOT/workouttimer-upload-key.jks"
    echo "🔑 Found fallback keystore: workouttimer-upload-key.jks"
  elif [ -f "release.jks" ]; then
    KEYSTORE_FILE="$ROOT/release.jks"
    echo "🔑 Found fallback keystore: release.jks"
  else
    echo "⚠️  Warning: No upload keystore found at root. Builds might use debug credentials or fail signing."
  fi
fi

if [ -n "$KEYSTORE_FILE" ] && [ -f "$KEYSTORE_FILE" ]; then
  echo "✅ Signing keystore active: $KEYSTORE_FILE"
fi

# 2. Run unit tests
if [ "$SKIP_TESTS" = false ]; then
  echo ""
  echo "🧪 Running JVM Unit Tests (testDebugUnitTest)..."
  ./gradlew testDebugUnitTest
  echo "✅ Unit tests passed!"
else
  echo "⏩ Skipping unit tests (--skip-tests specified)."
fi

# 3. Build signed Android App Bundle (.aab)
echo ""
echo "📦 Building signed release Android App Bundle (bundleRelease)..."
./gradlew bundleRelease

# 4. Build signed Release APK (.apk) for sideloading/verification
echo ""
echo "📱 Building signed release APK (assembleRelease)..."
./gradlew assembleRelease

AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
APK_PATH="app/build/outputs/apk/release/app-release.apk"

echo ""
echo "================================================================="
echo "🎉 Build Complete!"
echo "================================================================="
echo "• Android App Bundle (.aab): $AAB_PATH"
echo "• Release APK (.apk):        $APK_PATH"
echo "================================================================="

# 5. Publishing to Google Play Console
SERVICE_ACCOUNT=""
if [ -f "play-service-account.json" ]; then
  SERVICE_ACCOUNT="play-service-account.json"
elif [ -f "app/play-service-account.json" ]; then
  SERVICE_ACCOUNT="app/play-service-account.json"
fi

if [ "$BUNDLE_ONLY" = true ]; then
  echo ""
  echo "ℹ️  --bundle-only specified. Skipping upload to Play Console."
  echo "👉 To upload manually, drag and drop $AAB_PATH into Google Play Console."
  exit 0
fi

if [ -n "$SERVICE_ACCOUNT" ]; then
  echo ""
  echo "🚀 Service account found: $SERVICE_ACCOUNT"
  echo "🚀 Publishing release bundle to Google Play Console (Track: $TRACK)..."
  ./gradlew publishReleaseBundle --track="$TRACK"
  echo ""
  echo "🎉 Successfully published to Google Play Console ($TRACK track)!"
else
  echo ""
  echo "ℹ️  Service account key not found (play-service-account.json)."
  echo "👉 Next steps for Google Play Console Internal Testing:"
  echo "   1. Open Google Play Console: https://play.google.com/console"
  echo "   2. Navigate to Release > Testing > Internal testing."
  echo "   3. Create a new release and upload:"
  echo "      $AAB_PATH"
  echo ""
  echo "💡 For 1-click publishing with this script in the future:"
  echo "   Place your Google Play API Service Account JSON key at:"
  echo "   play-service-account.json (or app/play-service-account.json)"
  echo "   and re-run: ./scripts/publish-to-play-console.sh"
fi
