package org.example.air_workshop.di

import org.example.air_workshop.database.dao.ExpenseDao
import org.example.air_workshop.database.dao.GoalDao
import org.example.air_workshop.database.dao.SettingsDao
import org.example.air_workshop.database.dao.TagDao
import org.example.air_workshop.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<ExpenseRepository> { RoomExpenseRepository(get<ExpenseDao>()) }
    single<TagRepository> { RoomTagRepository(get<TagDao>()) }
    single<GoalRepository> { RoomGoalRepository(get<GoalDao>()) }
    single<SettingsRepository> { RoomSettingsRepository(get<SettingsDao>()) }
}
