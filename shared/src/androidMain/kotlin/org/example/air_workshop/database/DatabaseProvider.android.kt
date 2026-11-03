package org.example.air_workshop.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private var applicationContext: Context? = null

fun setAndroidContext(context: Context) {
    applicationContext = context.applicationContext
}

/** Application context set by [setAndroidContext]; shared with other androidMain platform code. */
internal fun requireAndroidContext(): Context = applicationContext
    ?: throw IllegalStateException("Android context not initialized. Call setAndroidContext() first.")

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = requireAndroidContext()
    val dbFile = context.getDatabasePath("expenses.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
