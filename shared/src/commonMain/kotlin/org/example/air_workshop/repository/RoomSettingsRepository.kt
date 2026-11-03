package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.air_workshop.database.dao.SettingsDao
import org.example.air_workshop.database.entities.SettingsEntity
import org.example.air_workshop.model.Currency

class RoomSettingsRepository(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    private companion object {
        const val KEY_DEFAULT_CURRENCY = "default_currency"
    }

    override fun observeDefaultCurrency(): Flow<Currency> =
        settingsDao.observeSetting(KEY_DEFAULT_CURRENCY)
            .map { entity ->
                entity?.value?.let { Currency.fromCode(it) } ?: Currency.EUR
            }

    override suspend fun getDefaultCurrency(): Currency {
        val entity = settingsDao.getSetting(KEY_DEFAULT_CURRENCY)
        return entity?.value?.let { Currency.fromCode(it) } ?: Currency.EUR
    }

    override suspend fun setDefaultCurrency(currency: Currency) {
        settingsDao.upsertSetting(
            SettingsEntity(
                key = KEY_DEFAULT_CURRENCY,
                value = currency.code
            )
        )
    }
}
