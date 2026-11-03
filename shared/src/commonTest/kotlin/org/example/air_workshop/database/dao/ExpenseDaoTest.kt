package org.example.air_workshop.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import org.example.air_workshop.database.entities.ExpenseEntity
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class ExpenseDaoTest {

    private val database = createInMemoryDatabase()
    private val dao = database.expenseDao()

    private fun createExpense(
        id: Long = 0L,
        amount: Double = 10.0,
        currencyCode: String = "EUR",
        timestamp: Long = Instant.fromEpochSeconds(1696118400).epochSeconds, // 2023-10-01
        tagId: Long = 1L,
        note: String = "Test expense",
        isRecurring: Boolean = false
    ) = ExpenseEntity(id, amount, currencyCode, timestamp, tagId, note, isRecurring)

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertExpense_returnsPositiveId() = runTest {
        val expense = createExpense()
        val id = dao.insertExpense(expense)
        assertTrue(id > 0, "Inserted expense should have a positive ID")
    }

    @Test
    fun insertAndGetExpenseById_returnsSameExpense() = runTest {
        val expense = createExpense(amount = 25.5, note = "Groceries")
        val id = dao.insertExpense(expense)
        val fetched = dao.getExpenseById(id)
        assertNotNull(fetched)
        assertEquals(25.5, fetched.amount)
        assertEquals("EUR", fetched.currencyCode)
        assertEquals("Groceries", fetched.note)
        assertEquals(1L, fetched.tagId)
    }

    @Test
    fun getExpenseById_nonExistent_returnsNull() = runTest {
        val fetched = dao.getExpenseById(999L)
        assertNull(fetched)
    }

    @Test
    fun updateExpense_modifiesFields() = runTest {
        val expense = createExpense(note = "Original")
        val id = dao.insertExpense(expense)
        val updated = createExpense(id = id, amount = 50.0, note = "Updated")
        dao.updateExpense(updated)
        val fetched = dao.getExpenseById(id)
        assertNotNull(fetched)
        assertEquals(50.0, fetched.amount)
        assertEquals("Updated", fetched.note)
    }

    @Test
    fun deleteExpense_removesRecord() = runTest {
        val expense = createExpense()
        val id = dao.insertExpense(expense)
        dao.deleteExpense(expense.copy(id = id))
        val fetched = dao.getExpenseById(id)
        assertNull(fetched)
    }

    @Test
    fun deleteExpenseById_removesRecord() = runTest {
        val expense = createExpense()
        val id = dao.insertExpense(expense)
        dao.deleteExpenseById(id)
        val fetched = dao.getExpenseById(id)
        assertNull(fetched)
    }

    @Test
    fun observeAllExpenses_returnsAllInserted() = runTest {
        dao.insertExpense(createExpense(amount = 10.0))
        dao.insertExpense(createExpense(amount = 20.0))
        dao.insertExpense(createExpense(amount = 30.0))
        val all = dao.observeAllExpenses().first()
        assertEquals(3, all.size)
    }

    @Test
    fun observeAllExpenses_orderedByTimestampDesc() = runTest {
        val now = Instant.fromEpochSeconds(1696118400)
        val earlier = Instant.fromEpochSeconds(1696032000)
        dao.insertExpense(createExpense(timestamp = now.epochSeconds, amount = 10.0))
        dao.insertExpense(createExpense(timestamp = earlier.epochSeconds, amount = 20.0))
        val all = dao.observeAllExpenses().first()
        assertEquals(2, all.size)
        assertEquals(10.0, all[0].amount) // newer first
        assertEquals(20.0, all[1].amount)
    }

    @Test
    fun getExpensesByMonth_returnsOnlyMatchingMonth() = runTest {
        // October 2023
        val oct = Instant.fromEpochSeconds(1696118400) // 2023-10-01
        // November 2023
        val nov = Instant.fromEpochSeconds(1698796800) // 2023-11-01
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, amount = 10.0))
        dao.insertExpense(createExpense(timestamp = nov.epochSeconds, amount = 20.0))
        val octExpenses = dao.getExpensesByMonth("2023", "10")
        assertEquals(1, octExpenses.size)
        assertEquals(10.0, octExpenses[0].amount)
    }

    @Test
    fun getTotalSpentByMonth_sumsCorrectly() = runTest {
        val oct = Instant.fromEpochSeconds(1696118400)
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, amount = 10.0))
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, amount = 20.0))
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, amount = 30.0))
        val total = dao.getTotalSpentByMonth("2023", "10")
        assertNotNull(total)
        assertEquals(60.0, total)
    }

    @Test
    fun getTotalSpentByMonth_empty_returnsNull() = runTest {
        val total = dao.getTotalSpentByMonth("2023", "10")
        assertNull(total)
    }

    @Test
    fun getRecentExpenses_limitsResults() = runTest {
        repeat(15) { i ->
            dao.insertExpense(createExpense(amount = i.toDouble(), note = "Item $i"))
        }
        val recent = dao.getRecentExpenses(limit = 5)
        assertEquals(5, recent.size)
    }

    @Test
    fun searchExpenses_byNote_returnsMatching() = runTest {
        dao.insertExpense(createExpense(note = "Grocery shopping"))
        dao.insertExpense(createExpense(note = "Restaurant dinner"))
        dao.insertExpense(createExpense(note = "Grocery delivery"))
        val results = dao.searchExpenses("Grocery").first()
        assertEquals(2, results.size)
    }

    @Test
    fun searchExpenses_emptyQuery_returnsAll() = runTest {
        dao.insertExpense(createExpense(note = "A"))
        dao.insertExpense(createExpense(note = "B"))
        val results = dao.searchExpenses("").first()
        assertEquals(2, results.size)
    }

    @Test
    fun observeExpensesByTag_returnsMatchingTag() = runTest {
        val oct = Instant.fromEpochSeconds(1696118400)
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, tagId = 1L, amount = 10.0))
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, tagId = 2L, amount = 20.0))
        dao.insertExpense(createExpense(timestamp = oct.epochSeconds, tagId = 1L, amount = 30.0))
        val tag1Expenses = dao.observeExpensesByTag(1L).first()
        assertEquals(2, tag1Expenses.size)
    }

    @Test
    fun insertExpense_withReplace_overwritesExistingId() = runTest {
        val expense1 = createExpense(id = 100L, amount = 10.0, note = "First")
        val expense2 = createExpense(id = 100L, amount = 20.0, note = "Second")
        dao.insertExpense(expense1)
        dao.insertExpense(expense2)
        val fetched = dao.getExpenseById(100L)
        assertNotNull(fetched)
        assertEquals(20.0, fetched.amount)
        assertEquals("Second", fetched.note)
    }
}
