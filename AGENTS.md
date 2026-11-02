# Agent Directives & Architecture Guardrails

## Core Principles
1. **Target Common First:** All domain models, ViewModels, business logic, and UI components MUST reside in `commonMain` unless platform-specific APIs are strictly required.
2. **No Platform Leaks:** Never import `android.*`, `java.*`, or platform-specific libraries into `commonMain`. Use `expect`/`actual` declarations only when strictly necessary.
3. **Never Edit Gradle Unprompted:** Do not modify `build.gradle.kts` or `libs.versions.toml` unless the current task explicitly requires adding a dependency.
4. **Compile-Check Every Step:** After generating code, verify compilation using the Gradle CLI (`./gradlew assembleDebug` or target-specific check).

## Tech Stack & Conventions
- **Language:** Kotlin 2.x
- **UI:** Compose Multiplatform (Material 3)
- **Architecture:** MVVM with MVI-style UI state: `StateFlow<UiState>` + single entry `onEvent(UiEvent)`.
- **Database:** Room KMP / SQLite
- **Dependency Injection:** Koin
- **Date/Time:** `kotlinx-datetime` (always use `Instant` or `LocalDate`, never `java.time.*` or `Date`).
- **Navigation:** Voyager or Navigation Compose (Single-activity / single-window).

## Working Process
- Work on **one task at a time** from `TASKS.md`.
- Mark completed tasks as `[x]` upon verifying that the code compiles.
- Do not refactor unrelated files outside the scope of the active task.