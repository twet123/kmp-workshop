package org.example.air_workshop.database

import androidx.room.TypeConverter
import kotlin.time.Instant

object Converters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.epochSeconds
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochSeconds(it) }
    }
}
