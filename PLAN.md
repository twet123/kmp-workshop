# Multiplatform Expense Tracker Implementation Plan

## Phase 0: Scaffolding & Build Configuration
- [x] Configure `gradle/libs.versions.toml` with versions and dependencies, please use only the latest stable version for these dependencies and avoid using 'RC', 'Alpha', 'Beta' and similar:
  - Compose Multiplatform (Material 3)
  - Room KMP (with SQLite driver and KSP compiler)
  - Koin (`koin-core`, `koin-compose`, `koin-compose-viewmodel`)
  - `kotlinx-datetime` and `kotlinx-coroutines-core`
  - Navigation library (Voyager or Navigation Compose Multiplatform)
  - Charting library (Vico Compose Multiplatform or Compose-Charts)
- [x] Verify baseline build and execute `./gradlew build` cleanly on all targets.

---

## Phase 1: Shared Domain Models & Local Persistence
- [ ] Define shared domain entities in `commonMain`:
  - `Expense` (id, amount, currencyCode, timestamp, tagId, note, isRecurring)
  - `Tag` (id, name, colorHex)
  - `MonthlyGoal` (monthYear, targetAmount, currencyCode)
  - `Currency` enum/value object (code, symbol, displayName)
- [ ] Implement Room database schema and entities:
  - `ExpenseEntity`, `TagEntity`, and `MonthlyGoalEntity` with indexes and relationships
  - Room TypeConverters for `Instant` / `LocalDateTime` via `kotlinx-datetime`
- [ ] Create Data Access Objects (DAOs):
  - `ExpenseDao`: CRUD operations, observe by month, observe by tag, search by query, delete by ID
  - `TagDao`: CRUD operations, observe all tags
  - `GoalDao`: upsert goal by `monthYear`, observe goal for month
- [ ] Implement platform-specific database path builders (`expect`/`actual`):
  - `commonMain`: `expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>`
  - `androidMain`: Resolve path via `context.getDatabasePath("expenses.db")`
  - `iosMain`: Resolve path in `NSDocumentDirectory` via `NSFileManager`
  - `desktopMain`: Resolve path inside user home directory (e.g., `~/.expense_tracker/expenses.db`)
- [ ] Configure database factory in `commonMain`:
  - Attach unified `BundledSQLiteDriver()` to `RoomDatabase.Builder`
  - Set query coroutine context (`Dispatchers.IO`)
  - Instantiate shared `AppDatabase` instance
- [ ] Implement repository interfaces and Room implementations:
  - `ExpenseRepository`
  - `TagRepository`
  - `GoalRepository`
  - `SettingsRepository` (local key-value store for user default currency)
- [ ] Write unit tests verifying Room DAOs, queries, and repository CRUD logic in `commonTest`.

---

## Phase 2: Domain Business Logic & Use Cases
- [ ] Implement `GetMonthlyExpensesUseCase`: fetches all expenses for a specified `year-month` grouped chronologically.
- [ ] Implement `GetTagBreakdownUseCase`: aggregates monthly spending grouped by tag (sum per tag, percentage of total).
- [ ] Implement `CalculateGoalProgressUseCase`: calculates total spent vs. `MonthlyGoal`, remaining balance, and warning threshold states (e.g., normal, warning at 80%, exceeded at 100%).
- [ ] Implement `SearchAndFilterExpensesUseCase`: filters expenses by search text (in notes), tag IDs, and date intervals.
- [ ] Implement `ExportMonthCsvUseCase`: formats all transactions of a given month into standard RFC 4180 CSV string format.
- [ ] Implement recurring expense evaluation logic: checks and auto-generates recurring transactions for the current active month.
- [ ] Set up Koin dependency injection modules:
  - `databaseModule` (Room instance, DAOs)
  - `repositoryModule` (Repositories)
  - `useCaseModule` (Business use cases)
  - `viewModelModule` (Screen ViewModels)

---

## Phase 3: Design System & Shared UI Components
- [ ] Set up Material 3 theme, dark/light color schemes, and shared typography.
- [ ] Build `MonthSelectorBar`:
  - Month and year title display (e.g., "November 2026")
  - Previous (`<`) and Next (`>`) navigation buttons
  - Quick-jump to current month action
- [ ] Build `GoalProgressBar`:
  - Visual linear indicator showing total spent vs. monthly goal
  - Color state changes (green for safe, amber for near-limit, red for exceeded)
  - Numeric display of spent amount, target goal, and percentage
- [ ] Build `TagExpenseChart`:
  - Pie/donut chart or horizontal segmented bar chart using Vico / multiplatform charts
  - Color-coded segments matching tag `colorHex`
  - Legend showing tag name, amount, and percentage
- [ ] Build `ExpenseItemRow`:
  - Visual layout: tag color indicator/chip, note/description, timestamp, and formatted currency amount
  - Swipe-to-delete action
  - Swipe-to-edit action (or click to open editor)
- [ ] Build `TagChip` and `TagPicker`:
  - Horizontal scrollable row or flow row of selectable tags
  - Selected vs. unselected states
- [ ] Build `ColorPickerRow`:
  - Preset palette of distinct hex colors for tag creation/editing

---

## Phase 4: Screen Implementation & ViewModels
- [ ] **Dashboard Screen (`DashboardScreen` & `DashboardViewModel`):**
  - Integrate `MonthSelectorBar` at the top
  - Display `GoalProgressBar` card
  - Display `TagExpenseChart` spending breakdown
  - Display "Recent Activity" list (latest 5–10 transactions) with a "View All" button
  - Floating Action Button (FAB) linking to `ExpenseEntryScreen`
  - Empty state when no expenses exist for the selected month
- [ ] **Expense Entry / Editor Screen (`ExpenseEntryScreen` & `ExpenseEntryViewModel`):**
  - Numeric input field for transaction amount with decimal validation
  - Currency picker dropdown (defaults to user's base currency setting)
  - Tag selector using `TagPicker`
  - Date picker dialog/field to backdate or forward-date the entry
  - Notes / description single-line text input
  - Recurring expense toggle switch ("Repeat monthly")
  - Save button, Cancel button, and Delete button (visible only in edit mode)
  - Form validation (non-zero amount, required tag selection)
- [ ] **Transaction History Screen (`HistoryScreen` & `HistoryViewModel`):**
  - Top search bar for filtering transactions by note/keyword
  - Horizontal filter chips for tag selection and date sub-ranges
  - Sticky header list grouping expenses chronologically by day
  - Embed `ExpenseItemRow` with swipe actions (swipe left to delete, swipe right to edit)
  - Empty search/filter result state
- [ ] **Settings & Configuration Screen (`SettingsScreen` & `SettingsViewModel`):**
  - **Goal Configuration:** Input field to set or update the monthly spending goal for the active month
  - **Default Currency:** Dropdown selector to choose global default currency (EUR, USD, RSD, etc.)
  - **Tag Management Sub-view:**
    - List of existing tags with edit and delete controls
    - "Add Tag" modal/form with name input and `ColorPickerRow`
  - **Data Export:** "Export Month as CSV" button triggering file export

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