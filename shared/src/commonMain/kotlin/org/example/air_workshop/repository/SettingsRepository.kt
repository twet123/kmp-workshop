package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.model.Currency

interface SettingsRepository {
    fun observeDefaultCurrency(): Flow<Currency>
    suspend fun getDefaultCurrency(): Currency
    suspend fun setDefaultCurrency(currency: Currency)
}
