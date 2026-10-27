package br.com.paulodiogo.orcamento.domain

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.ExpenseTagCrossRef
import br.com.paulodiogo.orcamento.domain.parseMoneyDigitsToCents
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseOperationsTest {
    private lateinit var database: BudgetDatabase
    private lateinit var operations: ExpenseOperations

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BudgetDatabase::class.java,
        ).build()
        operations = ExpenseOperations(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun createsFindsUpdatesAndDeletesExpenseWithoutTags() = runBlocking {
        val created = operations.create("  Mercado  ", 1_250L, "2026-10-27", now = 10L)

        assertEquals("Mercado", operations.findById(created.id)?.description)
        val updated = operations.update(created, description = "Farmácia", amountCents = 2_000L, now = 20L)
        assertEquals("Farmácia", operations.findById(created.id)?.description)
        assertEquals(2_000L, updated.amountCents)
        assertTrue(operations.delete(created.id))
        assertNull(operations.findById(created.id))
        assertFalse(operations.delete(created.id))
    }

    @Test
    fun createsReadsUpdatesAndClearsMultipleTagAssociationsAtomically() = runBlocking {
        val first = TagOperations(database).createOrReuse("Casa")
        val second = TagOperations(database).createOrReuse("Mercado")
        val expense = operations.create("Compra", 500L, "2026-10-27", listOf(first.id, second.id))

        operations.updateTags(expense.id, listOf(first.id, second.id))
        assertEquals(setOf(first.id, second.id), operations.findTagIds(expense.id).toSet())

        operations.updateTags(expense.id, emptyList())
        assertTrue(operations.findTagIds(expense.id).isEmpty())
        assertNotNull(operations.findById(expense.id))
    }

    @Test
    fun rejectsMissingTagWithoutChangingExistingAssociations() = runBlocking {
        val expense = operations.create("Compra", 500L, "2026-10-27")
        val tag = TagOperations(database).createOrReuse("Casa")
        operations.updateTags(expense.id, listOf(tag.id))

        try {
            operations.updateTags(expense.id, listOf("missing"))
            throw AssertionError("Missing tag should be rejected")
        } catch (_: IllegalStateException) {
            // Expected referential validation.
        }

        assertEquals(listOf(tag.id), operations.findTagIds(expense.id))
    }

    @Test
    fun rejectsInvalidExpenseAndPreservesExistingState() = runBlocking {
        val created = operations.create("Compra", 500L, "2026-10-27")

        try {
            operations.update(created, amountCents = 0L)
            throw AssertionError("Invalid amount should be rejected")
        } catch (_: DomainError.InvalidAmount) {
            // Expected domain validation.
        }

        assertNotNull(operations.findById(created.id))
        assertEquals(500L, operations.findById(created.id)?.amountCents)
    }

    @Test
    fun persistsCentDigitsAsExactExpenseCents() = runBlocking {
        val expense = operations.create("Compra", parseMoneyDigitsToCents("12345"), "2026-10-27")

        assertEquals(12_345L, operations.findById(expense.id)?.amountCents)
    }
}
