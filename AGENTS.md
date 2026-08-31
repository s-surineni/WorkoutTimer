# LLM Engineering Guidelines & Review Standards

Any LLM assistant, code reviewer, or developer working on or reviewing the **WorkoutTimer** codebase must adhere to and evaluate changes against the following core engineering principles:

---

## 1. Architecture & State Management (Unidirectional Data Flow)
- **Single Source of Truth (SSOT)**:
  - **Data Layer (Repository)**: Must be the authoritative source of truth. Expose read-only `Flow<T>` or `StateFlow<T>` and perform mutations atomically using thread-safe operators (e.g., `_stateFlow.update { ... }`).
- **ViewModel Layer**:
  - Transform repository flows into a single, cohesive, sealed UI state interface (`Loading`, `Success`, `Error`).
  - Use `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InitialState)` to manage subscription lifecycles safely across configuration changes and process death.
  - Expose UI action methods that delegate directly to domain/repository layers.
- **UI Layer**:
  - Collect state using `collectAsStateWithLifecycle()` to prevent unnecessary background processing when the app is in the background.
  - Composable screens must remain purely declarative and hoist all events upward via lambda callbacks (`onItemClick`, `onAddClick`, `onDelete`, `onStartWorkout`).
- **Data Modeling & Immutability**:
  - Keep domain/data models immutable (`@Immutable data class`).
  - Always provide unique, stable identifiers (e.g. `val id: String = UUID.randomUUID().toString()`) for list items to support `LazyColumn` keying and item animations.

---

## 2. Jetpack Compose & Material 3 Best Practices
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
  - Every UI component and screen should provide `@Preview` composables covering normal, empty, and edge-case states.

---

## 3. Accessibility (a11y) & Mobile Ergonomics
- **Semantic Content Descriptions**:
  - Interactive elements (icon buttons, floating action buttons) must provide descriptive `contentDescription` attributes (e.g., `"Add Tabata Workout"`, `"Delete Routine"`, `"Close Timer"`).
  - Purely decorative icons must have `contentDescription = null` to avoid screen reader clutter.
- **Touch Target Sizing**:
  - Ensure all interactive elements meet the minimum touch target area of **48dp × 48dp**.
- **Color Contrast & Dynamic Typography**:
  - Use high-contrast tokens (`primaryContainer` / `onPrimaryContainer`, `surfaceVariant` / `onSurfaceVariant`) conforming to WCAG AA guidelines.

---

## 4. Form Design & Usability
- **Input Validation & UX**:
  - Validate text inputs inline (e.g. non-empty names, positive duration numbers) and show descriptive `supportingText` and `isError` flags.
  - Provide input sanitization (e.g. `it.filter { it.isDigit() }` for numeric fields).
  - Configure `KeyboardOptions` with appropriate `KeyboardType` (e.g. `KeyboardType.Number`) and proper `ImeAction` flows (`ImeAction.Next` $\rightarrow$ `ImeAction.Done`).
  - Include quick preset chips (`SuggestionChip` in a `FlowRow`) for common duration selections (e.g. `15s`, `30s`, `45s`, `60s`).

---

## 5. Concurrency, Coroutines & Threading
- **Structured Concurrency**:
  - Never use `GlobalScope`. Always scope coroutines to `viewModelScope`, `rememberCoroutineScope()`, or `LaunchedEffect`.
- **Dispatcher Isolation**:
  - Run CPU-heavy or disk/network operations on appropriate dispatchers (`Dispatchers.Default` / `Dispatchers.IO`) via `withContext`.
  - Never block threads with `Thread.sleep()`; always use non-blocking `delay()`.

---

## 6. Testing Standards
- **Unit Tests (JVM)**:
  - All ViewModels and Repositories must be covered by JUnit tests in `app/src/test/`.
  - Use `kotlinx-coroutines-test` with a `MainDispatcherRule` (`StandardTestDispatcher`) and `advanceUntilIdle()` to ensure deterministic coroutine execution.
  - Test all UI state transitions: initial `Loading`, `Success` with populated data, state after additions, state after deletions, and timer state switches.
- **Instrumented UI Tests**:
  - Add/maintain Compose UI tests in `app/src/androidTest/` using `createAndroidComposeRule`.
  - Verify node hierarchy, text content, and interactive buttons using semantics and content descriptions.
- **Zero Warnings**:
  - Ensure zero compilation warnings or deprecations during Kotlin compilation (`./gradlew compileDebugKotlin compileDebugUnitTestKotlin`).

---

## 7. Categorized Review Checklist for LLMs & Developers
When reviewing or submitting pull requests / code changes in this repository, check against this list:

### Architecture & UDF
- [ ] Does the change maintain Unidirectional Data Flow (Repository $\rightarrow$ ViewModel $\rightarrow$ UI)?
- [ ] Are repository mutations atomic and thread-safe (`_stateFlow.update { ... }`)?
- [ ] Is UI state exposed as a single sealed interface via `stateIn(WhileSubscribed(5000))`?
- [ ] Are UI composables purely declarative with event hoisting?

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
- [ ] Does the code compile cleanly with no deprecation warnings?
- [ ] Has the build been verified using `./scripts/gradle-assemble-install-if-changed.sh` or `./gradlew assembleDebug`?
