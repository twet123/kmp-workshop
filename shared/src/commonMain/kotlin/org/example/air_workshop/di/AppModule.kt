package org.example.air_workshop.di

import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule = module {
    // ViewModels will be added in Phase 4 when screens are implemented
}

val allModules: List<Module> = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
