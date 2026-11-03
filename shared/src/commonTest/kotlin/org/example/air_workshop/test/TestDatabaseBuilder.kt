package org.example.air_workshop.test

import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import org.example.air_workshop.database.AppDatabase

/**
 * Base class for tests that need a Room database.
 * On Android host tests it runs the class under Robolectric so a Context is available.
 */
expect abstract class RoomTest()

/** Platform in-memory builder, with the SQLite driver already configured where needed. */
expect fun createInMemoryDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createInMemoryDatabase(): AppDatabase {
    return createInMemoryDatabaseBuilder()
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
