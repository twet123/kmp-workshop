# Multiplatform Expense Tracker Implementation Plan

## Phase 0: Scaffolding & Build Configuration
- [x] Configure `gradle/libs.versions.toml` with versions and dependencies, please use only the latest stable version for these dependencies and avoid using 'RC', 'Alpha', 'Beta' and similar:
  - Compose Multiplatform (Material 3)
  - Room KMP (with SQLite driver and KSP compiler)
  - Koin (`koin-core`, `koin-compose`)
  - `kotlinx-datetime` and `kotlinx-coroutines-core`
  - Navigation library (Voyager or Navigation Compose Multiplatform)
  - Charting library (Vico Compose Multiplatform or Compose-Charts)
- [x] Verify baseline build and execute `./gradlew build` cleanly on all targets.

---

## Phase 1: Shared Domain Models & Local Persistence
- [x] Define shared domain entities in `commonMain`:
  - `Expense` (id, amount, currencyCode, timestamp, tagId, note, isRecurring)
  - `Tag` (id, name, colorHex)
  - `MonthlyGoal` (monthYear, targetAmount, currencyCode)
  - `Currency` enum/value object (code, symbol, displayName)
- [x] Implement Room database schema and entities:
  - `ExpenseEntity`, `TagEntity`, and `MonthlyGoalEntity` with indexes and relationships
  - Room TypeConverters for `Instant` / `LocalDateTime` via `kotlinx-datetime`
- [x] Create Data Access Objects (DAOs):
  - `ExpenseDao`: CRUD operations, observe by month, observe by tag, search by query, delete by ID
  - `TagDao`: CRUD operations, observe all tags
  - `GoalDao`: upsert goal by `monthYear`, observe goal for month
- [x] Implement platform-specific database path builders (`expect`/`actual`):
  - `commonMain`: `expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>`
  - `androidMain`: Resolve path via `context.getDatabasePath("expenses.db")`
  - `iosMain`: Resolve path in `NSDocumentDirectory` via `NSFileManager`
  - `desktopMain`: Resolve path inside user home directory (e.g., `~/.expense_tracker/expenses.db`)
- [x] Configure database factory in `commonMain`:
  - Attach unified `BundledSQLiteDriver()` to `RoomDatabase.Builder`
  - Set query coroutine context (`Dispatchers.IO`)
  - Instantiate shared `AppDatabase` instance
- [x] Implement repository interfaces and Room implementations:
  - `ExpenseRepository`
  - `TagRepository`
  - `GoalRepository`
  - `SettingsRepository` (local key-value store for user default currency)
- [x] Write unit tests verifying Room DAOs, queries, and repository CRUD logic in `commonTest`.

---

## Phase 2: Domain Business Logic & MVI Foundation
- [x] Implement `GetMonthlyExpensesUseCase`: fetches all expenses for a specified `year-month` grouped chronologically.
- [x] Implement `GetTagBreakdownUseCase`: aggregates monthly spending grouped by tag (sum per tag, percentage of total).
- [x] Implement `CalculateGoalProgressUseCase`: calculates total spent vs. `MonthlyGoal`, remaining balance, and warning threshold states.
- [x] Implement `SearchAndFilterExpensesUseCase`: filters expenses by search text (in notes), tag IDs, and date intervals.
- [x] Implement `ExportMonthCsvUseCase`: formats all transactions of a given month into standard RFC 4180 CSV string format.
- [x] Implement recurring expense evaluation logic: auto-generates recurring transactions for the current active month.
- [x] Build base MVI abstractions in `commonMain`:
  - [x] `MviStore<State, Intent, Effect>` base class or interface using `StateFlow<State>` and `Channel`/`Flow<Effect>`
  - [x] Sealed `Intent` interface pattern for discrete user/lifecycle actions
  - [x] Immutable `State` data class pattern per screen
  - [x] Single-shot `Effect` pattern for navigation, snackbars, and other side effects
  - [x] Base `CoroutineScope` lifecycle handler for KMP desktop, mobile, and iOS runtimes
- [x] Set up Koin dependency injection modules:
  - [x] `databaseModule` (Room instance, DAOs)
  - [x] `repositoryModule` (Repositories)
  - [x] `useCaseModule` (Business use cases)
  - [x] `storeModule` (MVI Stores / State Containers per screen)

---

## Phase 3: Design System & Shared UI Components
- [x] Set up Material 3 theme, dark/light color schemes, and shared typography.
- [x] Build `MonthSelectorBar`:
  - Month and year title display (e.g., "November 2026")
  - Previous (`<`) and Next (`>`) navigation buttons
  - Quick-jump to current month action
- [x] Build `GoalProgressBar`:
  - Visual linear indicator showing total spent vs. monthly goal
  - Color state changes (green for safe, amber for near-limit, red for exceeded)
  - Numeric display of spent amount, target goal, and percentage
- [x] Build `TagExpenseChart`:
  - Pie/donut chart or horizontal segmented bar chart using Vico / multiplatform charts
  - Color-coded segments matching tag `colorHex`
  - Legend showing tag name, amount, and percentage
- [x] Build `ExpenseItemRow`:
  - Visual layout: tag color indicator/chip, note/description, timestamp, and formatted currency amount
  - Swipe-to-delete action
  - Swipe-to-edit action (or click to open editor)
- [x] Build `TagChip` and `TagPicker`:
  - Horizontal scrollable row or flow row of selectable tags
  - Selected vs. unselected states
- [x] Build `ColorPickerRow`:
  - Preset palette of distinct hex colors for tag creation/editing

---

## Phase 4: Screen Implementation & VMI Stores
- [ ] **Dashboard Screen:**
  - Define `DashboardState` (month, goal metrics, tag breakdown, recent list, loading/error states)
  - Define `DashboardIntent` (SelectMonth, DeleteExpense, Refresh)
  - Define `DashboardEffect` (one-off navigation events, snackbars)
  - Implement `DashboardStore` to process intents, call use cases, and reduce state
  - Implement `DashboardScreen` Compose view consuming `DashboardState` and routing intents to `DashboardStore.onIntent`
- [ ] **Expense Entry / Editor Screen:**
  - Define `ExpenseEntryState` (form fields, validation errors, available tags, selected currency, isEditMode)
  - Define `ExpenseEntryIntent` (UpdateAmount, SelectTag, SelectCurrency, SelectDate, ToggleRecurring, Submit, Delete)
  - Define `ExpenseEntryEffect` (DismissScreen, ShowValidationToast)
  - Implement `ExpenseEntryStore`
  - Implement `ExpenseEntryScreen` Compose view
- [ ] **Transaction History Screen:**
  - Define `HistoryState` (grouped expenses, active query, selected filter tags, active date range)
  - Define `HistoryIntent` (SearchQueryChanged, TagFilterToggled, DateRangeSelected, DeleteExpense)
  - Define `HistoryEffect` (NavigateToEditor)
  - Implement `HistoryStore`
  - Implement `HistoryScreen` with sticky daily headers and swipe actions
- [ ] **Settings & Configuration Screen:**
  - Define `SettingsState` (current monthly goal, base currency, tag list, new tag input modal state)
  - Define `SettingsIntent` (UpdateMonthlyGoal, SelectBaseCurrency, CreateTag, EditTag, DeleteTag, TriggerCsvExport)
  - Define `SettingsEffect` (LaunchShareSheet, ShowNotification)
  - Implement `SettingsStore`
  - Implement `SettingsScreen` with Tag CRUD forms and export triggers

---

## Phase 5: Navigation & Platform-Specific Integrations
- [ ] Set up main navigation structure:
  - Bottom navigation bar (Mobile) / Side navigation rail (Desktop) for Dashboard, History, and Settings
  - Modal navigation / push transitions for Expense Entry and Tag Editor
- [ ] Implement `expect`/`actual` CSV file exporter:
  - `androidMain`: writes temporary CSV file and opens `ACTION_SEND` Intent share sheet
  - `iosMain`: writes temporary file and launches `UIActivityViewController`
  - `desktopMain`: opens native AWT / Swing file save dialog (`JFileChooser`) to save `.csv` to local disk
- [ ] Desktop UX Polish:
  - Configure default window size, minimum bounds (e.g., 900x700), and window title in `desktopMain/kotlin/main.kt`
  - Add keyboard shortcuts (e.g., `Ctrl/Cmd + N` to create expense, `Ctrl/Cmd + F` to search)

---