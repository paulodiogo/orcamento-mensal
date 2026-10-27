package br.com.paulodiogo.orcamento.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.withTransaction
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetDatabaseTest {
    private lateinit var database: BudgetDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BudgetDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun persistsBudgetAndRejectsDuplicateMonth() {
        val budget = BudgetEntity("2026-10", 10_000L, 1L, 1L)
        runDatabase { budgetDao().insert(budget) }

        assertEquals(budget, runDatabase { budgetDao().findByMonth("2026-10") })
        try {
            runDatabase { budgetDao().insert(budget.copy(amountCents = 20_000L)) }
            throw AssertionError("Duplicate month should be rejected")
        } catch (_: Exception) {
            // Expected Room primary-key constraint.
        }
    }

    @Test
    fun keepsExpenseWithoutTagsAndRemovesTagAssociationsOnly() {
        val expense = ExpenseEntity("expense-1", "Mercado", 1_250L, "2026-10-27", 1L, 1L)
        val tag = TagEntity("tag-1", "Mercado", "mercado", 1L, 1L)
        runDatabase { expenseDao().insert(expense) }
        runDatabase { tagDao().insert(tag) }
        runDatabase { expenseTagDao().insert(ExpenseTagCrossRef(expense.id, tag.id)) }
        assertEquals(1, runDatabase { expenseTagDao().countForExpense(expense.id) })

        runDatabase {
            withTransaction {
                tagDao().deleteAssociations(tag.id)
                tagDao().deleteById(tag.id)
            }
        }

        assertNotNull(runDatabase { expenseDao().findById(expense.id) })
        assertEquals(0, runDatabase { expenseTagDao().countForExpense(expense.id) })
        assertNull(runDatabase { tagDao().findById(tag.id) })
    }

    private fun <T> runDatabase(block: suspend BudgetDatabase.() -> T): T =
        kotlinx.coroutines.runBlocking { block(database) }
}

