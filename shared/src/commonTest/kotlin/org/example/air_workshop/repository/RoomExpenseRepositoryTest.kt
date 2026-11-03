package org.example.air_workshop.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import org.example.air_workshop.model.Expense
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class RoomExpenseRepositoryTest {

    private val database = createInMemoryDatabase()
    private val repository = RoomExpenseRepository(database.expenseDao())

    private fun createExpense(
        id: Long = 0L,
        amount: Double = 10.0,
        currencyCode: String = "EUR",
        timestamp: Instant = Instant.fromEpochSeconds(1696118400), // 2023-10-01
        tagId: Long = 1L,
        note: String = "Test expense",
        isRecurring: Boolean = false
    ) = Expense(id, amount, currencyCode, timestamp, tagId, note, isRecurring)

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertExpense_returnsPositiveId() = runTest {
        val expense = createExpense()
        val id = repository.insertExpense(expense)
        assertTrue(id > 0)
    }

    @Test
    fun insertAndGetExpenseById_preservesDomainFields() = runTest {
        val expense = createExpense(amount = 42.5, currencyCode = "USD", note = "Lunch", isRecurring = true)
        val id = repository.insertExpense(expense)
        val fetched = repository.getExpenseById(id)
        assertNotNull(fetched)
        assertEquals(42.5, fetched.amount)
        assertEquals("USD", fetched.currencyCode)
        assertEquals("Lunch", fetched.note)
        assertTrue(fetched.isRecurring)
        assertEquals(expense.timestamp, fetched.timestamp)
    }

    @Test
    fun getExpenseById_nonExistent_returnsNull() = runTest {
        val fetched = repository.getExpenseById(999L)
        assertNull(fetched)
    }

    @Test
    fun updateExpense_modifiesDomainFields() = runTest {
        val expense = createExpense(note = "Original", amount = 10.0)
        val id = repository.insertExpense(expense)
        val updated = createExpense(id = id, amount = 99.9, note = "Updated", isRecurring = true)
        repository.updateExpense(updated)
        val fetched = repository.getExpenseById(id)
        assertNotNull(fetched)
        assertEquals(99.9, fetched.amount)
        assertEquals("Updated", fetched.note)
        assertTrue(fetched.isRecurring)
    }

    @Test
    fun deleteExpense_removesRecord() = runTest {
        val expense = createExpense()
        val id = repository.insertExpense(expense)
        repository.deleteExpense(expense.copy(id = id))
        assertNull(repository.getExpenseById(id))
    }

    @Test
    fun deleteExpenseById_removesRecord() = runTest {
        val expense = createExpense()
        val id = repository.insertExpense(expense)
        repository.deleteExpenseById(id)
        assertNull(repository.getExpenseById(id))
    }

    @Test
    fun observeAllExpenses_returnsAllInserted() = runTest {
        repository.insertExpense(createExpense(amount = 10.0))
        repository.insertExpense(createExpense(amount = 20.0))
        val all = repository.observeAllExpenses().first()
        assertEquals(2, all.size)
    }

    @Test
    fun observeAllExpenses_empty_returnsEmptyList() = runTest {
        val all = repository.observeAllExpenses().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun getExpensesByMonth_returnsOnlyMatchingMonth() = runTest {
        val oct = Instant.fromEpochSeconds(1696118400) // 2023-10-01
        val nov = Instant.fromEpochSeconds(1698796800) // 2023-11-01
        repository.insertExpense(createExpense(timestamp = oct, amount = 10.0))
        repository.insertExpense(createExpense(timestamp = nov, amount = 20.0))
        val octExpenses = repository.getExpensesByMonth("2023", "10")
        assertEquals(1, octExpenses.size)
        assertEquals(10.0, octExpenses[0].amount)
    }

    @Test
    fun getTotalSpentByMonth_sumsCorrectly() = runTest {
        val oct = Instant.fromEpochSeconds(1696118400)
        repository.insertExpense(createExpense(timestamp = oct, amount = 10.0))
        repository.insertExpense(createExpense(timestamp = oct, amount = 20.0))
        repository.insertExpense(createExpense(timestamp = oct, amount = 30.0))
        val total = repository.getTotalSpentByMonth("2023", "10")
        assertEquals(60.0, total)
    }

    @Test
    fun getTotalSpentByMonth_empty_returnsZero() = runTest {
        val total = repository.getTotalSpentByMonth("2023", "10")
        assertEquals(0.0, total)
    }

    @Test
    fun getRecentExpenses_limitsResults() = runTest {
        repeat(20) { i ->
            repository.insertExpense(createExpense(amount = i.toDouble()))
        }
        val recent = repository.getRecentExpenses(limit = 5)
        assertEquals(5, recent.size)
    }

    @Test
    fun searchExpenses_byNote_returnsMatching() = runTest {
        repository.insertExpense(createExpense(note = "Grocery shopping"))
        repository.insertExpense(createExpense(note = "Restaurant dinner"))
        repository.insertExpense(createExpense(note = "Grocery delivery"))
        val results = repository.searchExpenses("Grocery").first()
        assertEquals(2, results.size)
    }

    @Test
    fun observeExpensesByTag_returnsMatchingTag() = runTest {
        val oct = Instant.fromEpochSeconds(1696118400)
        repository.insertExpense(createExpense(timestamp = oct, tagId = 1L, amount = 10.0))
        repository.insertExpense(createExpense(timestamp = oct, tagId = 2L, amount = 20.0))
        repository.insertExpense(createExpense(timestamp = oct, tagId = 1L, amount = 30.0))
        val tag1Expenses = repository.observeExpensesByTag(1L).first()
        assertEquals(2, tag1Expenses.size)
    }

    @Test
    fun insertExpense_preservesTimestampConversion() = runTest {
        val specificTime = Instant.fromEpochSeconds(1696118400)
        val expense = createExpense(timestamp = specificTime)
        val id = repository.insertExpense(expense)
        val fetched = repository.getExpenseById(id)
        assertNotNull(fetched)
        assertEquals(specificTime, fetched.timestamp)
    }
}
