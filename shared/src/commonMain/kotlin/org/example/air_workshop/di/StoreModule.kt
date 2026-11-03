package org.example.air_workshop.di

import org.koin.dsl.module

val storeModule = module {
    // MVI Stores will be added in Phase 4 when screens are implemented
    // Example: factory { DashboardStore(get(), get(), get()) }
}
