package org.example.air_workshop.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.air_workshop.model.MonthlyGoal
import org.example.air_workshop.test.RoomTest
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class RoomGoalRepositoryTest : RoomTest() {

    private val database = createInMemoryDatabase()
    private val repository = RoomGoalRepository(database.goalDao())

    private fun createGoal(
        monthYear: String = "2023-10",
        targetAmount: Double = 1000.0,
        currencyCode: String = "EUR"
    ) = MonthlyGoal(monthYear, targetAmount, currencyCode)

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun upsertGoal_insertsNewGoal() = runTest {
        val goal = createGoal()
        repository.upsertGoal(goal)
        val fetched = repository.getGoalByMonth("2023-10")
        assertNotNull(fetched)
        assertEquals("2023-10", fetched.monthYear)
        assertEquals(1000.0, fetched.targetAmount)
        assertEquals("EUR", fetched.currencyCode)
    }

    @Test
    fun upsertGoal_updatesExistingGoal() = runTest {
        repository.upsertGoal(createGoal(targetAmount = 500.0))
        repository.upsertGoal(createGoal(monthYear = "2023-10", targetAmount = 750.0, currencyCode = "USD"))
        val fetched = repository.getGoalByMonth("2023-10")
        assertNotNull(fetched)
        assertEquals(750.0, fetched.targetAmount)
        assertEquals("USD", fetched.currencyCode)
    }

    @Test
    fun getGoalByMonth_nonExistent_returnsNull() = runTest {
        assertNull(repository.getGoalByMonth("2023-10"))
    }

    @Test
    fun observeGoalByMonth_returnsGoal() = runTest {
        repository.upsertGoal(createGoal(targetAmount = 2000.0))
        val observed = repository.observeGoalByMonth("2023-10").first()
        assertNotNull(observed)
        assertEquals(2000.0, observed.targetAmount)
    }

    @Test
    fun observeGoalByMonth_nonExistent_returnsNull() = runTest {
        val observed = repository.observeGoalByMonth("2023-10").first()
        assertNull(observed)
    }

    @Test
    fun deleteGoalByMonthYear_removesGoal() = runTest {
        repository.upsertGoal(createGoal())
        repository.deleteGoalByMonthYear("2023-10")
        assertNull(repository.getGoalByMonth("2023-10"))
    }

    @Test
    fun observeAllGoals_returnsAllGoals() = runTest {
        repository.upsertGoal(createGoal(monthYear = "2023-08", targetAmount = 800.0))
        repository.upsertGoal(createGoal(monthYear = "2023-09", targetAmount = 900.0))
        repository.upsertGoal(createGoal(monthYear = "2023-10", targetAmount = 1000.0))
        val all = repository.observeAllGoals().first()
        assertEquals(3, all.size)
    }

    @Test
    fun observeAllGoals_empty_returnsEmptyList() = runTest {
        val all = repository.observeAllGoals().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun observeAllGoals_orderedByMonthYearDesc() = runTest {
        repository.upsertGoal(createGoal(monthYear = "2023-10"))
        repository.upsertGoal(createGoal(monthYear = "2023-08"))
        repository.upsertGoal(createGoal(monthYear = "2023-09"))
        val all = repository.observeAllGoals().first()
        assertEquals("2023-10", all[0].monthYear)
        assertEquals("2023-09", all[1].monthYear)
        assertEquals("2023-08", all[2].monthYear)
    }

    @Test
    fun multipleGoals_differentMonths_areIndependent() = runTest {
        repository.upsertGoal(createGoal(monthYear = "2023-10", targetAmount = 1000.0, currencyCode = "EUR"))
        repository.upsertGoal(createGoal(monthYear = "2023-11", targetAmount = 1500.0, currencyCode = "USD"))
        val oct = repository.getGoalByMonth("2023-10")
        val nov = repository.getGoalByMonth("2023-11")
        assertNotNull(oct)
        assertNotNull(nov)
        assertEquals(1000.0, oct.targetAmount)
        assertEquals("EUR", oct.currencyCode)
        assertEquals(1500.0, nov.targetAmount)
        assertEquals("USD", nov.currencyCode)
    }
}
