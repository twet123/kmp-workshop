package org.example.air_workshop.di

import org.example.air_workshop.usecases.*
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetMonthlyExpensesUseCase(get()) }
    factory { GetTagBreakdownUseCase(get(), get()) }
    factory { CalculateGoalProgressUseCase(get(), get()) }
    factory { SearchAndFilterExpensesUseCase(get()) }
    factory { ExportMonthCsvUseCase(get(), get()) }
    factory { EvaluateRecurringExpensesUseCase(get()) }
}
