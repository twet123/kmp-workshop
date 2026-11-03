package org.example.air_workshop.test

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import org.example.air_workshop.database.AppDatabase
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
actual abstract class RoomTest actual constructor()

actual fun createInMemoryDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // No driver: the bundled SQLite native lib can't load on the host JVM,
    // so Room falls back to framework SQLite, which Robolectric provides.
    val context = ApplicationProvider.getApplicationContext<Context>()
    return Room.inMemoryDatabaseBuilder<AppDatabase>(context)
}
