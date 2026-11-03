package org.example.air_workshop.di

import org.example.air_workshop.database.AppDatabase
import org.example.air_workshop.database.provideDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> { provideDatabase() }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().settingsDao() }
}
