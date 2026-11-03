package org.example.air_workshop.di

import org.example.air_workshop.ui.screens.dashboard.DashboardStore
import org.example.air_workshop.ui.screens.expenseentry.ExpenseEntryStore
import org.example.air_workshop.ui.screens.history.HistoryStore
import org.example.air_workshop.ui.screens.settings.SettingsStore
import org.koin.dsl.module

val storeModule = module {
    factory { DashboardStore(
        get(), get(), get(), get(), get(), get(), get()
    ) }
    factory { ExpenseEntryStore(
        get(), get(), get()
    ) }
    factory { HistoryStore(
        get(), get(), get(), get()
    ) }
    factory { SettingsStore(
        get(), get(), get(), get()
    ) }
}
