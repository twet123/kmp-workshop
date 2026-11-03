# Agent Directives & Architecture Guardrails

## Project & Domain Context
- **App Concept:** A cross-platform personal expense and budget tracker for a given month and year.
- **Primary Goal:** Allow users to log daily expenses, categorize them with tags, monitor their monthly spending against a target budget goal, and inspect visual breakdowns per category.
- **Core Entities & Domain Rules:**
  - `Expense`: A recorded monetary transaction containing an amount, currency, timestamp/date, associated tag, optional description note, and a recurring flag.
  - `Tag`: A category used to classify expenses (e.g., Groceries, Rent, Utilities). Each tag has a user-defined name and an assigned hex color for UI identification.
  - `MonthlyGoal`: A target spending ceiling set for a specific calendar month (e.g., "2026-11"). Used to compute budget progress and threshold warnings.
  - `Currency`: Multi-currency selection per transaction (e.g., EUR, USD, RSD). Base currency determines default display values.
- **Platform Scope:** Shared single-codebase UI targeting Android, iOS, and Desktop (JVM) using Compose Multiplatform.

## Core Principles
1. **Target Common First:** All domain models, MVI Stores, business logic, and UI components MUST reside in `commonMain` unless platform-specific APIs are strictly required.
2. **No Platform Leaks:** Never import `android.*`, `java.*`, or platform-specific libraries into `commonMain`. Use `expect`/`actual` declarations only when strictly necessary. Do not use any platform specific calls in common* sources, like "%.2f".format(), it's a Java specific call.
3. **Never Edit Gradle Unprompted:** Do not modify `build.gradle.kts` or `libs.versions.toml` unless the current task explicitly requires adding a dependency.
4. **Compile-Check Every Step:** After generating code, verify compilation using the Gradle CLI (`./gradlew assembleDebug` or target-specific check).

## Tech Stack & Conventions
- **Architecture:** MVI (Model-View-Intent) using MVI Stores.
  - **View:** Declarative Compose functions that collect `StateFlow<State>` and emit `Intent`s.
  - **Intent:** Sealed interfaces representing discrete user/lifecycle actions (never expose loose public mutation methods).
  - **Store:** Explicit state container (`DashboardStore`, `SettingsStore`) containing a single immutable `State` data class and a reducer/intent handler.
  - **Side Effects:** Single-shot events (e.g., navigation, file saving) dispatched via a shared Kotlin `Channel` / `Flow<Effect>`.
- **Language:** Kotlin 2.x
- **UI:** Compose Multiplatform (Material 3)
- **Database:** Room KMP / SQLite
- **Dependency Injection:** Koin
- **Date/Time:** `kotlinx-datetime` (always use `Instant` or `LocalDate`, never `java.time.*` or `Date`). But, prefer using kotlin.time if the same API is available in it, because kotlinx.datetime was mostly deprecated in favor of kotlin.time
- **Navigation:** Voyager or Navigation Compose (Single-activity / single-window).

## Working Process
- Work on **one task at a time** from `TASKS.md`.
- Mark completed tasks as `[x]` upon verifying that the code compiles.
- Do not refactor unrelated files outside the scope of the active task.