package org.example.air_workshop.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.ui.screens.dashboard.DashboardScreen
import org.example.air_workshop.ui.screens.dashboard.DashboardStore
import org.example.air_workshop.ui.screens.expenseentry.ExpenseEntryScreen
import org.example.air_workshop.ui.screens.expenseentry.ExpenseEntryStore
import org.example.air_workshop.ui.screens.history.HistoryScreen
import org.example.air_workshop.ui.screens.history.HistoryStore
import org.example.air_workshop.ui.screens.settings.SettingsScreen
import org.example.air_workshop.ui.screens.settings.SettingsStore
import org.koin.compose.koinInject

object Routes {
    const val Dashboard = "dashboard"
    const val History = "history"
    const val Settings = "settings"
    const val ExpenseEntry = "expense_entry"
    const val ExpenseEntryEdit = "expense_entry/{expenseId}"
    const val ExpenseIdArg = "expenseId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    dashboardStore: DashboardStore = koinInject(),
    historyStore: HistoryStore = koinInject(),
    settingsStore: SettingsStore = koinInject(),
    onCsvExport: (fileName: String, csvContent: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard,
        modifier = modifier
    ) {
        composable(Routes.Dashboard) {
            DashboardScreen(
                store = dashboardStore,
                onNavigateToExpenseEntry = { editExpenseId ->
                    if (editExpenseId != null && editExpenseId > 0) {
                        navController.navigate("${Routes.ExpenseEntry}/$editExpenseId")
                    } else {
                        navController.navigate(Routes.ExpenseEntry)
                    }
                }
            )
        }

        composable(Routes.History) {
            HistoryScreen(
                store = historyStore,
                onNavigateToEditor = { expenseId ->
                    navController.navigate("${Routes.ExpenseEntry}/$expenseId")
                }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                store = settingsStore,
                onCsvExport = onCsvExport
            )
        }

        composable(Routes.ExpenseEntry) {
            val entryStore = koinInject<ExpenseEntryStore>()
            ExpenseEntryScreen(
                store = entryStore,
                onDismiss = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ExpenseEntryEdit,
            arguments = listOf(navArgument(Routes.ExpenseIdArg) {
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.read { getStringOrNull(Routes.ExpenseIdArg) }?.toLongOrNull() ?: return@composable
            val entryStore = koinInject<ExpenseEntryStore>()
            val expenseRepository = koinInject<ExpenseRepository>()

            LaunchedEffect(expenseId) {
                val expense = expenseRepository.getExpenseById(expenseId)
                expense?.let { entryStore.setExpenseToEdit(it) }
            }

            ExpenseEntryScreen(
                store = entryStore,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
