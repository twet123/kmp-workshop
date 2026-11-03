package org.example.air_workshop.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.air_workshop.model.Currency
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class RoomSettingsRepositoryTest {

    private val database = createInMemoryDatabase()
    private val repository = RoomSettingsRepository(database.settingsDao())

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun getDefaultCurrency_default_returnsEur() = runTest {
        val currency = repository.getDefaultCurrency()
        assertEquals(Currency.EUR, currency)
    }

    @Test
    fun setDefaultCurrency_andRetrieve_returnsSetCurrency() = runTest {
        repository.setDefaultCurrency(Currency.USD)
        val currency = repository.getDefaultCurrency()
        assertEquals(Currency.USD, currency)
    }

    @Test
    fun setDefaultCurrency_updatesToNewValue() = runTest {
        repository.setDefaultCurrency(Currency.USD)
        assertEquals(Currency.USD, repository.getDefaultCurrency())
        repository.setDefaultCurrency(Currency.RSD)
        assertEquals(Currency.RSD, repository.getDefaultCurrency())
    }

    @Test
    fun observeDefaultCurrency_default_returnsEur() = runTest {
        val currency = repository.observeDefaultCurrency().first()
        assertEquals(Currency.EUR, currency)
    }

    @Test
    fun observeDefaultCurrency_afterSet_returnsSetCurrency() = runTest {
        repository.setDefaultCurrency(Currency.GBP)
        val currency = repository.observeDefaultCurrency().first()
        assertEquals(Currency.GBP, currency)
    }

    @Test
    fun setDefaultCurrency_withVariousCurrencies_persistsCorrectly() = runTest {
        val currencies = listOf(Currency.USD, Currency.EUR, Currency.RSD, Currency.PLN, Currency.JPY)
        currencies.forEach { currency ->
            repository.setDefaultCurrency(currency)
            assertEquals(currency, repository.getDefaultCurrency())
        }
    }

    @Test
    fun setDefaultCurrency_invalidCode_fallsBackToEur() = runTest {
        // Directly insert an invalid currency code via the DAO to test fallback
        database.settingsDao().upsertSetting(
            org.example.air_workshop.database.entities.SettingsEntity(
                key = "default_currency",
                value = "INVALID"
            )
        )
        val currency = repository.getDefaultCurrency()
        assertEquals(Currency.EUR, currency)
    }

    @Test
    fun observeDefaultCurrency_empty_returnsEur() = runTest {
        // Fresh database with no settings
        val currency = repository.observeDefaultCurrency().first()
        assertEquals(Currency.EUR, currency)
    }
}
