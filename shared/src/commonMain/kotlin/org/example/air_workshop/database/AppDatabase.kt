package org.example.air_workshop.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.example.air_workshop.database.dao.ExpenseDao
import org.example.air_workshop.database.dao.GoalDao
import org.example.air_workshop.database.dao.SettingsDao
import org.example.air_workshop.database.dao.TagDao
import org.example.air_workshop.database.entities.ExpenseEntity
import org.example.air_workshop.database.entities.MonthlyGoalEntity
import org.example.air_workshop.database.entities.SettingsEntity
import org.example.air_workshop.database.entities.TagEntity

@Database(
    entities = [ExpenseEntity::class, TagEntity::class, MonthlyGoalEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun tagDao(): TagDao
    abstract fun goalDao(): GoalDao
    abstract fun settingsDao(): SettingsDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
