package br.com.paulodiogo.orcamento.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.domain.BudgetOperations
import br.com.paulodiogo.orcamento.domain.parseMoneyDigitsToCents
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetOperationsTest {
    private lateinit var database: BudgetDatabase
    private lateinit var operations: BudgetOperations

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        operations = BudgetOperations(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun suggestsPreviousBudgetAndKeepsMonthsIndependent() = runBlocking {
        operations.save("2026-09", 100_000L, now = 1L)
        val suggestion = operations.suggestedAmountFor("2026-10")
        assertEquals(100_000L, suggestion)

        operations.save("2026-10", 75_000L, now = 2L)
        assertEquals(75_000L, operations.findByMonth("2026-10")?.amountCents)
        assertEquals(100_000L, operations.findByMonth("2026-09")?.amountCents)
    }

    @Test
    fun editsExistingMonthWithoutCreatingDuplicate() = runBlocking {
        operations.save("2026-10", 100L, now = 1L)
        operations.save("2026-10", 200L, now = 2L)
        assertEquals(200L, operations.findByMonth("2026-10")?.amountCents)
        assertEquals(1, database.budgetDao().count())
        assertNull(operations.findByMonth("2026-11"))
    }

    @Test
    fun acceptsZeroAndPersistsCentDigitsAsExactBudgetCents() = runBlocking {
        operations.save("2026-10", parseMoneyDigitsToCents("0"), now = 1L)
        assertEquals(0L, operations.findByMonth("2026-10")?.amountCents)

        operations.save("2026-11", parseMoneyDigitsToCents("12345"), now = 2L)
        assertEquals(12_345L, operations.findByMonth("2026-11")?.amountCents)
    }
}
