package org.example.air_workshop.test

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.example.air_workshop.database.AppDatabase

expect fun createInMemoryDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createInMemoryDatabase(): AppDatabase {
    return createInMemoryDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
