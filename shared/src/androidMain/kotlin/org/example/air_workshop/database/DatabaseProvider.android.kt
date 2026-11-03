package org.example.air_workshop.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private var applicationContext: Context? = null

fun setAndroidContext(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = applicationContext
        ?: throw IllegalStateException("Android context not initialized. Call setAndroidContext() first.")
    val dbFile = context.getDatabasePath("expenses.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
