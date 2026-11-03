package org.example.air_workshop.di

import org.koin.core.module.Module

val allModules: List<Module> = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    storeModule
)
