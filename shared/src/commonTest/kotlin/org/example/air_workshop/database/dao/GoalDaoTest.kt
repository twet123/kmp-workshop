package org.example.air_workshop.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.air_workshop.database.entities.MonthlyGoalEntity
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class GoalDaoTest {

    private val database = createInMemoryDatabase()
    private val dao = database.goalDao()

    private fun createGoal(
        monthYear: String = "2023-10",
        targetAmount: Double = 1000.0,
        currencyCode: String = "EUR"
    ) = MonthlyGoalEntity(monthYear, targetAmount, currencyCode)

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun upsertGoal_insertsNewGoal() = runTest {
        val goal = createGoal()
        dao.upsertGoal(goal)
        val fetched = dao.getGoalByMonth("2023-10")
        assertNotNull(fetched)
        assertEquals("2023-10", fetched.monthYear)
        assertEquals(1000.0, fetched.targetAmount)
        assertEquals("EUR", fetched.currencyCode)
    }

    @Test
    fun upsertGoal_updatesExistingGoal() = runTest {
        val goal1 = createGoal(targetAmount = 500.0)
        dao.upsertGoal(goal1)
        val goal2 = createGoal(monthYear = "2023-10", targetAmount = 750.0, currencyCode = "USD")
        dao.upsertGoal(goal2)
        val fetched = dao.getGoalByMonth("2023-10")
        assertNotNull(fetched)
        assertEquals(750.0, fetched.targetAmount)
        assertEquals("USD", fetched.currencyCode)
    }

    @Test
    fun getGoalByMonth_nonExistent_returnsNull() = runTest {
        val fetched = dao.getGoalByMonth("2023-10")
        assertNull(fetched)
    }

    @Test
    fun observeGoalByMonth_returnsGoal() = runTest {
        val goal = createGoal(targetAmount = 2000.0)
        dao.upsertGoal(goal)
        val observed = dao.observeGoalByMonth("2023-10").first()
        assertNotNull(observed)
        assertEquals(2000.0, observed.targetAmount)
    }

    @Test
    fun observeGoalByMonth_nonExistent_returnsNull() = runTest {
        val observed = dao.observeGoalByMonth("2023-10").first()
        assertNull(observed)
    }

    @Test
    fun deleteGoalByMonthYear_removesGoal() = runTest {
        val goal = createGoal()
        dao.upsertGoal(goal)
        dao.deleteGoalByMonthYear("2023-10")
        val fetched = dao.getGoalByMonth("2023-10")
        assertNull(fetched)
    }

    @Test
    fun observeAllGoals_returnsAllGoals() = runTest {
        dao.upsertGoal(createGoal(monthYear = "2023-08", targetAmount = 800.0))
        dao.upsertGoal(createGoal(monthYear = "2023-09", targetAmount = 900.0))
        dao.upsertGoal(createGoal(monthYear = "2023-10", targetAmount = 1000.0))
        val all = dao.observeAllGoals().first()
        assertEquals(3, all.size)
    }

    @Test
    fun observeAllGoals_orderedByMonthYearDesc() = runTest {
        dao.upsertGoal(createGoal(monthYear = "2023-10", targetAmount = 1000.0))
        dao.upsertGoal(createGoal(monthYear = "2023-08", targetAmount = 800.0))
        dao.upsertGoal(createGoal(monthYear = "2023-09", targetAmount = 900.0))
        val all = dao.observeAllGoals().first()
        assertEquals("2023-10", all[0].monthYear)
        assertEquals("2023-09", all[1].monthYear)
        assertEquals("2023-08", all[2].monthYear)
    }

    @Test
    fun observeAllGoals_empty_returnsEmptyList() = runTest {
        val all = dao.observeAllGoals().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun multipleGoals_differentMonths_areIndependent() = runTest {
        dao.upsertGoal(createGoal(monthYear = "2023-10", targetAmount = 1000.0, currencyCode = "EUR"))
        dao.upsertGoal(createGoal(monthYear = "2023-11", targetAmount = 1500.0, currencyCode = "USD"))
        val oct = dao.getGoalByMonth("2023-10")
        val nov = dao.getGoalByMonth("2023-11")
        assertNotNull(oct)
        assertNotNull(nov)
        assertEquals(1000.0, oct.targetAmount)
        assertEquals("EUR", oct.currencyCode)
        assertEquals(1500.0, nov.targetAmount)
        assertEquals("USD", nov.currencyCode)
    }
}
