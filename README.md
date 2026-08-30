# WorkoutTimer

An Android application built with modern Android development standards using Jetpack Compose, Kotlin Coroutines, and Material 3.

---

## Prerequisites

- **Java Development Kit (JDK):** Version 17+
- **Android SDK:** Platform API 36, Build Tools 36
- **Android CLI** (or Android Studio)

---

## Setup & Running the App

### 1. Create and Start the Emulator

For the fastest development speed and lowest resource overhead, use the `small_phone` profile:

```bash
# Create the lightweight emulator
android emulator create small_phone

# Start the emulator (Quick Boot is enabled by default)
android emulator start small_phone
```

### 2. Build & Launch the Application

From the project root, use Gradle to build and install the app on the active emulator:

```bash
# Build the debug APK
./gradlew assembleDebug

# Install the app on the currently connected emulator/device
./gradlew installDebug

# If you want a faster incremental install during development
./gradlew installDebug --rerun-tasks
```

### 3. Fast Rebuild & Install Script

Use the helper script to automatically check for changes, build, and install only when necessary:

```bash
./scripts/gradle-assemble-install-if-changed.sh
```

If you prefer to use the Android CLI directly, it requires an APK path, for example:

```bash
android run --apks app/build/outputs/apk/debug/app-debug.apk
```

---

## Testing

The project includes both **local unit tests** and **instrumented UI tests**.

### 1. Running Local Unit Tests (JVM)

Local unit tests run on your local machine's JVM without requiring an emulator or device.

To run all unit tests:
```bash
./gradlew test
```

To run only the debug unit tests:
```bash
./gradlew testDebugUnitTest
```

To run a specific test class:
```bash
./gradlew testDebugUnitTest --tests "com.example.workouttimer.ui.main.MainScreenViewModelTest"
```

To run a specific test method:
```bash
./gradlew testDebugUnitTest --tests "com.example.workouttimer.ui.main.MainScreenViewModelTest.uiState_initiallyLoading"
```

#### Viewing Unit Test Reports
After running tests, an HTML report is generated at:
```text
app/build/reports/tests/testDebugUnitTest/index.html
```

---

### 2. Running Instrumented & UI Tests (On Emulator/Device)

Instrumented tests run on a live Android device or running emulator.

1. **Ensure the emulator is running:**
   ```bash
   android emulator start small_phone
   ```

2. **Run the instrumented tests:**
   ```bash
   ./gradlew connectedAndroidTest
   ```

To run a specific instrumented test class:
```bash
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.workouttimer.ui.main.MainScreenTest
```

#### Viewing Instrumented Test Reports
After the test run finishes, view the HTML report at:
```text
app/build/reports/androidTests/connected/index.html
```

---

## Project Structure

- `app/src/main/java/com/example/workouttimer/` — Application source code (UI, ViewModels, Data layer, Theme)
- `app/src/test/java/com/example/workouttimer/` — Local unit tests (ViewModel and Repository tests)
- `app/src/androidTest/java/com/example/workouttimer/` — Compose UI & instrumented tests

---

## Engineering Guidelines & AI Review Standards

This project maintains strict engineering standards regarding Unidirectional Data Flow (UDF), Material 3 best practices, coroutine lifecycle safety, and deterministic testing.

See **[AGENTS.md](AGENTS.md)** for the complete guide and AI code review checklist.

