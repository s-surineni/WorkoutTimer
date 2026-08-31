# LLM Engineering Guidelines & Review Standards

Any LLM assistant, code reviewer, or developer working on or reviewing the **WorkoutTimer** codebase must adhere to and evaluate changes against the following core engineering principles:

---

## 1. Architecture & State Management (Unidirectional Data Flow)
- **Single Source of Truth (SSOT)**:
  - **Data Layer (Repository)**: Must be the authoritative source of truth. Expose read-only `Flow<T>` or `StateFlow<T>` backed by persistent storage or state holders, and perform mutations atomically.
- **ViewModel Layer**:
  - Transform repository flows into a single, cohesive, sealed UI state interface (`Loading`, `Success`, `Error`).
  - Use `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InitialState)` to manage subscription lifecycles safely across configuration changes and process death.
  - Expose UI action methods that delegate directly to domain/repository layers.
- **UI Layer**:
  - Collect state using `collectAsStateWithLifecycle()` to prevent unnecessary background processing when the app is in the background.
  - Composable screens must remain purely declarative and hoist all events upward via lambda callbacks (`onItemClick`, `onAddClick`, `onEditClick`, `onDelete`, `onStartWorkout`).
- **Data Modeling & Immutability**:
  - Keep domain/data models immutable (`@Immutable data class`).
  - Always provide unique, stable identifiers (e.g. `val id: String = UUID.randomUUID().toString()`) for list items to support `LazyColumn` keying and item animations.

---

## 2. Database & Local Persistence Standards (Room & KSP)
- **Entity & Domain Separation**:
  - Keep Room `@Entity` data classes strictly decoupled from domain models.
  - Provide explicit bidirectional mapping extension functions (e.g., `Workout.toEntity()` and `WorkoutEntity.toDomain()`).
- **Reactive Data Access (DAOs)**:
  - Query methods returning collections must expose read-only `Flow<List<T>>` for automatic, reactive UI updates upon table modifications.
  - Perform write operations (`@Insert`, `@Update`, `@Delete`, `@Query`) cleanly with proper return types and dispatch them off the main thread.
- **Type Converters & Serialization**:
  - Convert complex embedded types (such as `List<Exercise>`) to/from JSON text columns using `kotlinx.serialization.json.Json`.
  - Type converters must include fallback exception handling so malformed records do not crash queries.
- **Pre-seeding & Migration Safety**:
  - Initialize default routines/presets via `RoomDatabase.Callback` on `onCreate` executed within background coroutines (`Dispatchers.IO`).
- **Thread & Dispatcher Safety**:
  - All database write transactions in repositories must execute on `Dispatchers.IO` via structured coroutines.

---

## 3. Audio, Haptics & Media Lifecycle Standards
- **Interface Abstraction**:
  - Audio and haptic feedback managers must be defined via interfaces (e.g., `AudioFeedbackManager`).
  - Always provide lightweight `NoOp` implementations (e.g., `NoOpAudioFeedbackManager`) to allow Compose `@Preview`s and JVM unit tests to execute without native hardware dependencies.
- **Deterministic Resource Lifecycle**:
  - Encapsulate native audio hardware (such as `ToneGenerator`, `SoundPool`, or `MediaPlayer`) in `DisposableEffect` with `onDispose { manager.release() }` or ViewModel `onCleared()` to prevent resource leaks.
- **Fail-Safe & Non-Blocking Triggers**:
  - Audio playback calls must execute asynchronously and be guarded against runtime hardware exceptions so audio failures never interrupt timer progression.
- **User Mute Controls & Semantics**:
  - All screens generating audio must expose user-toggleable mute/unmute controls with dynamic, accessible `contentDescription`s (`"Mute Sound"` / `"Unmute Sound"`).

---

## 4. Jetpack Compose & Material 3 Best Practices
- **Material 3 Ecosystem**:
  - Rely exclusively on standard Material 3 components (`Scaffold`, `TopAppBar`, `FloatingActionButton`, `AlertDialog`, `Card`, `LinearProgressIndicator`, `SuggestionChip`, etc.).
  - Never hardcode ad-hoc colors or typography; always leverage `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- **Recomposition Optimization**:
  - **Lambda-based state reads**: For frequently changing values (e.g. timer progress, counters), use lambda overloads like `LinearProgressIndicator(progress = { progressRatio })` to avoid recomposing parent containers.
  - **Stable List Keys**: Always supply explicit, stable keys in lazy lists: `items(items, key = { it.id })`.
  - **Derived State**: Wrap computed values that depend on snapshot state in `remember { derivedStateOf { ... } }` to minimize recomposition frequency.
  - **Callback Stability**: Use `rememberUpdatedState` when capturing callbacks inside long-running coroutines or effects to avoid stale captures without restarting effects.
- **Lifecycle & Side-Effect Safety**:
  - Encapsulate timers, delays, and continuous loops within `LaunchedEffect` with explicit key dependencies so coroutines cancel automatically when leaving the composition.
- **Compose Previews**:
  - Every UI component and screen should provide `@Preview` composables covering normal, empty, creation, and edit states.

---

## 5. Accessibility (a11y) & Mobile Ergonomics
- **Semantic Content Descriptions**:
  - Interactive elements (icon buttons, floating action buttons) must provide descriptive `contentDescription` attributes (e.g., `"Add Tabata Workout"`, `"Edit Routine"`, `"Delete Routine"`, `"Mute Sound"`, `"Navigate back"`).
  - Purely decorative icons must have `contentDescription = null` to avoid screen reader clutter.
- **Touch Target Sizing**:
  - Ensure all interactive elements meet the minimum touch target area of **48dp × 48dp**.
- **Color Contrast & Dynamic Typography**:
  - Use high-contrast tokens (`primaryContainer` / `onPrimaryContainer`, `surfaceVariant` / `onSurfaceVariant`) conforming to WCAG AA guidelines.

---

## 6. Form Design & Usability
- **Input Validation & UX**:
  - Validate text inputs inline (e.g. non-empty names, positive duration numbers) and show descriptive `supportingText` and `isError` flags.
  - Provide input sanitization (e.g. `it.filter { it.isDigit() }` for numeric fields).
  - Configure `KeyboardOptions` with appropriate `KeyboardType` (e.g. `KeyboardType.Number`) and proper `ImeAction` flows (`ImeAction.Next` $\rightarrow$ `ImeAction.Done`).
  - Include quick preset chips (`SuggestionChip` in a `FlowRow`) for common duration selections (e.g. `15s`, `30s`, `45s`, `60s`).

---

## 7. Concurrency, Coroutines & Threading
- **Structured Concurrency**:
  - Never use `GlobalScope`. Always scope coroutines to `viewModelScope`, `rememberCoroutineScope()`, or `LaunchedEffect`.
- **Dispatcher Isolation**:
  - Run CPU-heavy or disk/network operations on appropriate dispatchers (`Dispatchers.Default` / `Dispatchers.IO`) via `withContext`.
  - Never block threads with `Thread.sleep()`; always use non-blocking `delay()`.

---

## 8. Testing Standards
- **Unit Tests (JVM)**:
  - All ViewModels, domain logic, Audio Managers, and Repositories must be covered by JUnit tests in `app/src/test/`.
  - Use `kotlinx-coroutines-test` with a `MainDispatcherRule` (`StandardTestDispatcher`) and `advanceUntilIdle()` to ensure deterministic coroutine execution.
  - Test all UI state transitions: initial `Loading`, `Success` with populated data, state after additions, state after edits, state after deletions, and timer state switches.
- **Instrumented Room & UI Tests**:
  - Test Room database operations and DAOs in `app/src/androidTest/` using in-memory databases (`Room.inMemoryDatabaseBuilder`).
  - Add/maintain Compose UI tests in `app/src/androidTest/` using `createComposeRule` verifying node hierarchies, text content, interactive buttons, and sound toggles.
- **Zero Warnings**:
  - Ensure zero compilation warnings or deprecations during Kotlin compilation (`./gradlew compileDebugKotlin compileDebugUnitTestKotlin compileDebugAndroidTestKotlin`).

---

## 9. Categorized Review Checklist for LLMs & Developers
When reviewing or submitting pull requests / code changes in this repository, check against this list:

### Architecture & UDF
- [ ] Does the change maintain Unidirectional Data Flow (Repository $\rightarrow$ ViewModel $\rightarrow$ UI)?
- [ ] Are repository mutations atomic and thread-safe?
- [ ] Is UI state exposed as a single sealed interface via `stateIn(WhileSubscribed(5000))`?
- [ ] Are UI composables purely declarative with event hoisting?

### Database & Persistence (Room)
- [ ] Are Room `@Entity` data classes decoupled from domain models with explicit mappers?
- [ ] Do DAOs return reactive `Flow<List<T>>` for observable queries?
- [ ] Are disk writes and mutations dispatched to `Dispatchers.IO`?
- [ ] Are complex types safely converted via TypeConverters with error fallbacks?
- [ ] Are database operations covered by in-memory instrumented tests (`Room.inMemoryDatabaseBuilder`)?

### Audio, Haptics & Media
- [ ] Are audio/media managers abstracted behind interfaces with NoOp implementations for previews and tests?
- [ ] Are native audio resources released deterministically via `DisposableEffect` / `onDispose` or ViewModel `onCleared()`?
- [ ] Are audio playback calls non-blocking and guarded against hardware exceptions?
- [ ] Are user mute/unmute controls provided with dynamic accessibility labels?

### Compose Performance & Recomposition
- [ ] Are list items uniquely keyed in lazy layouts (`key = { it.id }`)?
- [ ] Are high-frequency state reads passed via lambdas rather than direct values?
- [ ] Are side-effects correctly scoped in `LaunchedEffect` with appropriate keys?
- [ ] Are `@Preview`s provided for all components and screen states?

### Accessibility & Ergonomics
- [ ] Do all interactive icon buttons have descriptive `contentDescription`s?
- [ ] Are decorative icons marked with `contentDescription = null`?
- [ ] Do touch targets meet the 48dp minimum dimension?

### Forms & Input Validation
- [ ] Are text inputs validated with user-friendly error messages and `supportingText`?
- [ ] Are numeric inputs sanitized with appropriate `KeyboardOptions`?
- [ ] Are preset suggestion chips provided for common duration selections?

### Concurrency & Lifecycle
- [ ] Is structured concurrency preserved without `GlobalScope` or blocking calls?
- [ ] Are coroutines automatically cancelled when leaving composition?

### Testing & Quality
- [ ] Are unit tests updated and passing (`./gradlew testDebugUnitTest`)?
- [ ] Are instrumented UI & Room tests passing (`./gradlew connectedAndroidTest`)?
- [ ] Does the code compile cleanly with no deprecation warnings?
- [ ] Has the build been verified using `./scripts/gradle-assemble-install-if-changed.sh` or `./gradlew assembleDebug`?
