package org.example.air_workshop.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.database.entities.SettingsEntity

@Dao
interface SettingsDao {

    @Upsert
    suspend fun upsertSetting(setting: SettingsEntity)

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingsEntity?

    @Query("SELECT * FROM settings WHERE key = :key")
    fun observeSetting(key: String): Flow<SettingsEntity?>
}
