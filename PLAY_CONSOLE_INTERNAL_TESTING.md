# Google Play Console Internal Testing Guide for WorkoutTimer

This guide walks you through publishing the **WorkoutTimer** app to Google Play Console's **Internal Testing** track or distributing it directly to testers.

---

## 1. Built Artifacts

Both production-ready, signed artifacts are compiled during the release workflow:

| Artifact | Location | Usage |
| :--- | :--- | :--- |
| **Android App Bundle (.aab)** | `app/build/outputs/bundle/release/app-release.aab` | **Required** for Google Play Console |
| **Release APK (.apk)** | `app/build/outputs/apk/release/app-release.apk` | For direct sideloading or local testing |

---

## 2. Keystore & Signing Configuration

- **Keystore File**: `workouttimer-upload-key.jks` (kept in project root, gitignored)
- **Configuration File**: `keystore.properties` (gitignored)
- **Key Alias**: `workouttimer_upload`
- **Key & Store Password**: `workouttimerInternalPass2026`
- **Algorithm**: 2048-bit RSA with 27-year validity

> [!CAUTION]
> **Backup your keystore!** Store a copy of `workouttimer-upload-key.jks` and `keystore.properties` in a secure password manager or vault. If you lose this key, you will not be able to push updates to Google Play without submitting a key reset request to Google Play Console support.

---

## 3. Step-by-Step: Setting Up Google Play Internal Testing

### Step 1: Create the App in Google Play Console
1. Log in to the [Google Play Console](https://play.google.com/console).
2. Click **Create app**.
3. Fill in:
   - **App name**: `WorkoutTimer`
   - **Default language**: English (United States)
   - **App or game**: App
   - **Free or paid**: Free
4. Accept the declarations and click **Create app**.

### Step 2: Complete Required Policy Declarations (Initial Setup)
Before you can publish on any track (even Internal Testing), Play Console requires completing the basic declarations under **Policy and programs** > **App content**:
- **Privacy policy**: URL to your privacy policy.
- **Ads**: Declare whether your app contains ads (No).
- **App access**: Declare if parts of the app are restricted (All functional without restrictions).
- **Content ratings**: Fill out the brief rating questionnaire.
- **Target audience and content**: Select target age (e.g. 18 and over).
- **Data safety**: Declare data collection (WorkoutTimer stores workout routines and history locally on-device via Room; no data collected or transmitted).

### Step 3: Store Listing Graphics (Available in Repository)
High-resolution assets are already prepared in the repository:
- **App Icon (512x512 PNG)**: `store_assets/icon_512x512.png`
- **Feature Graphic (1024x500 PNG)**: `store_assets/feature_graphic_1024x500.png`

### Step 4: Set Up Internal Testing Track
1. In the left navigation menu, go to **Release** > **Testing** > **Internal testing**.
2. Click **Create new release** in the top right.
3. **App signing by Google Play**: Ensure Play App Signing is enabled (default).
4. **App bundles**: Drag and drop `app/build/outputs/bundle/release/app-release.aab`.
5. **Release name**: Enter `1.0.1 (2) - Initial Internal Test`.
6. **Release notes**: Add brief notes for testers (e.g., `Initial internal testing release of WorkoutTimer with HIIT & Tabata routines`).
7. Click **Next** > **Save and publish**.

### Step 5: Add Internal Testers & Share the Link
1. In **Internal testing**, click the **Testers** tab.
2. Under **Email lists**, click **Create email list** (e.g., "Internal Team").
3. Add tester Gmail / Google Workspace email addresses.
4. Save the list and ensure it is selected for the internal testing track.
5. Under **How testers join your test**, copy the **Join on Android** or **Join on the web** link.
6. Send this link to your testers to install the app directly from the Google Play Store.

---

## 4. Automated 1-Click Publishing via Gradle Play Publisher (GPP)

WorkoutTimer includes the **Gradle Play Publisher** plugin configured in `app/build.gradle.kts`.

### Setting Up API Access:
1. In Google Play Console, go to **Setup** > **API access**.
2. Link or create a Google Cloud Project.
3. Under **Service accounts**, click **Create service account** and follow Google Cloud Console instructions to create a service account with **Service Account User** role.
4. In Google Cloud Console, create a new **JSON key** for that service account and download it.
5. In Play Console **API access**, click **Grant access** for that service account, and under **Permissions**, grant **Release apps to testing tracks**.
6. Save the downloaded JSON file as `play-service-account.json` in the root of `WorkoutTimer` (or inside `app/`).

Once configured, publish updates directly from your terminal:
```bash
./scripts/publish-to-play-console.sh
```

---

## 5. Alternative: Direct Sideload Testing

To install and test the signed release APK directly onto a connected physical device or emulator without using Play Console:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 6. Useful Build & Publish Commands

```bash
# Automated release script (tests, builds signed AAB/APK, and publishes if key exists)
./scripts/publish-to-play-console.sh

# Build signed bundle and APK without publishing
./scripts/publish-to-play-console.sh --bundle-only

# Publish to specific track (e.g. alpha, beta, internal)
./scripts/publish-to-play-console.sh --track alpha

# Using bare Gradle commands:
./gradlew testDebugUnitTest       # Run JVM unit tests
./gradlew bundleRelease           # Build signed Android App Bundle (.aab)
./gradlew assembleRelease         # Build signed Release APK (.apk)
./gradlew publishReleaseBundle    # Direct GPP publish to Play Console
```

> [!TIP]
> Remember to increment `versionCode` (e.g., `3`) and `versionName` (e.g., `"1.0.2"`) in [`app/build.gradle.kts`](file:///home/sampath/projects/WorkoutTimer/app/build.gradle.kts) before uploading new releases to Play Console.
