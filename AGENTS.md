# LLM Engineering Guidelines & Review Standards

Any LLM assistant or developer working on or reviewing the **WorkoutTimer** codebase must adhere to and review against the following core engineering principles:

---

## 1. Architecture & State Management (Unidirectional Data Flow)
- **Unidirectional Data Flow (UDF)**:
  - **Data Layer (Repository)**: Must be the single source of truth. Expose read-only `Flow`/`StateFlow` and atomic mutation methods using thread-safe operators (`_stateFlow.update { ... }`).
  - **ViewModel Layer**: Transform repository flows into a single sealed UI state interface (`Loading`, `Success`, `Error`). Use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InitialState)` to manage subscription lifecycles safely across configuration changes.
  - **UI Layer**: Collect state using `collectAsStateWithLifecycle()`. Composable screens must remain purely declarative and hoist events upward via lambda callbacks (`onItemClick`, `onAddClick`, `onDelete`).
- **Data Modeling**:
  - Keep domain/data models immutable (`data class`).
  - Always provide unique, stable identifiers (e.g. `val id: String = UUID.randomUUID().toString()`) for list items to support `LazyColumn` keying and item animations.

---

## 2. Jetpack Compose & Material 3 Best Practices
- **Material 3 Ecosystem**:
  - Rely on standard Material 3 components (`Scaffold`, `TopAppBar`, `FloatingActionButton`, `AlertDialog`, `Card`, `LinearProgressIndicator`, etc.).
  - Never hardcode ad-hoc colors or typography; always leverage `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- **Recomposition Optimization**:
  - Use lambda-based state reads for frequently changing UI properties (e.g. `LinearProgressIndicator(progress = { progressRatio })`) to avoid unnecessary recomposition passes of parent containers.
  - Always supply explicit, stable keys in lazy lists: `items(items, key = { it.id })`.
- **Lifecycle & Side-Effect Safety**:
  - Encapsulate timers, delays, and continuous loops within `LaunchedEffect` with explicit key dependencies so coroutines cancel automatically when leaving the composition.
- **Compose Previews**:
  - Every UI component and screen should provide `@Preview` composables, including normal, empty, and edge-case states.

---

## 3. Form Design & Mobile Usability
- **Input Validation & UX**:
  - Validate text inputs inline (e.g. non-empty names, positive duration numbers) and show descriptive `supportingText` and `isError` flags.
  - Provide input sanitization (e.g. `it.filter { it.isDigit() }` for numeric fields).
  - Configure `KeyboardOptions` with appropriate `KeyboardType` (e.g. `KeyboardType.Number`) and proper `ImeAction` flows (`ImeAction.Next` $\rightarrow$ `ImeAction.Done`).
  - Include quick preset chips (`SuggestionChip` in a `FlowRow`) for common duration selections (e.g. `15s`, `30s`, `45s`, `60s`).

---

## 4. Testing Standards
- **Unit Tests (JVM)**:
  - All ViewModels and Repositories must be covered by JUnit tests in `app/src/test/`.
  - Use `kotlinx-coroutines-test` with a `MainDispatcherRule` (`StandardTestDispatcher` / `UnconfinedTestDispatcher`) to ensure deterministic coroutine execution.
  - Test all UI state transitions: initial `Loading`, `Success` with populated data, state after additions, and state after deletions.
- **Instrumented UI Tests**:
  - Add/maintain Compose UI tests in `app/src/androidTest/` using `createAndroidComposeRule`.
  - Verify node hierarchy, text content, and interactive buttons using semantics and content descriptions.
- **Zero Warnings**:
  - Ensure zero compilation warnings or deprecations during Kotlin compilation (`./gradlew compileDebugKotlin compileDebugUnitTestKotlin`).

---

## 5. Review Checklist for LLMs
When reviewing or submitting pull requests / code changes in this repository, check against this list:
- [ ] Does the change maintain Unidirectional Data Flow (Repository $\rightarrow$ ViewModel $\rightarrow$ UI)?
- [ ] Are list items uniquely keyed in lazy layouts?
- [ ] Are animations or high-frequency state reads passed via lambdas rather than direct values where supported?
- [ ] Are text inputs validated with user-friendly error messages and correct keyboard options?
- [ ] Are empty and error states handled in UI screens?
- [ ] Are unit tests updated and passing (`./gradlew testDebugUnitTest`)?
- [ ] Does the code compile cleanly with no deprecation warnings?
- [ ] Has the build been verified using `./scripts/gradle-assemble-install-if-changed.sh` or `./gradlew assembleDebug`?

