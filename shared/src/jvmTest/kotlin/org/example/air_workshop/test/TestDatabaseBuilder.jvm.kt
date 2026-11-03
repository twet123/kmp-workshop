package org.example.air_workshop.test

import androidx.room.Room
import androidx.room.RoomDatabase
import org.example.air_workshop.database.AppDatabase

actual fun createInMemoryDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.inMemoryDatabaseBuilder<AppDatabase>()
}
