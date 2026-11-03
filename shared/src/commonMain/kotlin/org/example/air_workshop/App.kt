package org.example.air_workshop

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.air_workshop.navigation.AppNavGraph
import org.example.air_workshop.navigation.Routes
import org.example.air_workshop.platform.shareCsvFile
import org.example.air_workshop.ui.theme.AppTheme
import kotlinx.coroutines.launch

data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val BottomNavItems = listOf(
    NavItem(Routes.Dashboard, "Dashboard", Icons.Default.Home),
    NavItem(Routes.History, "History", Icons.Default.History),
    NavItem(Routes.Settings, "Settings", Icons.Default.Settings)
)

@Composable
fun App(isDesktop: Boolean = false) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Dashboard
    // The expense entry screen has its own submit button; the global FAB would cover it
    val showAddFab = BottomNavItems.any { it.route == currentRoute }

    AppTheme {
        when {
            isDesktop -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Side navigation rail for desktop
                    NavigationRail(
                        modifier = Modifier.width(80.dp)
                    ) {
                        BottomNavItems.forEach { item ->
                            NavigationRailItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.Dashboard) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }

                    // FAB for adding expenses
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph(
                            navController = navController,
                            onCsvExport = { fileName, csvContent ->
                                scope.launch {
                                    shareCsvFile(fileName, csvContent)
                                }
                            }
                        )

                        if (showAddFab) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(Routes.ExpenseEntry)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add expense"
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                // Bottom navigation bar for mobile
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            BottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(Routes.Dashboard) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (showAddFab) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(Routes.ExpenseEntry)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add expense"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    AppNavGraph(
                        navController = navController,
                        onCsvExport = { fileName, csvContent ->
                            scope.launch {
                                shareCsvFile(fileName, csvContent)
                            }
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}