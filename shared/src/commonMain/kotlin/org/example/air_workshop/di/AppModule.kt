package org.example.air_workshop.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

val allModules: List<Module> = listOf(
    databaseModule,
    repositoryModule,
    useCaseModule,
    storeModule
)

/** Starts Koin with all app modules; no-op if Koin is already running. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin {
        appDeclaration()
        modules(allModules)
    }
}
