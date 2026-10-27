package br.com.paulodiogo.orcamento.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.domain.DomainError
import br.com.paulodiogo.orcamento.domain.TagOperations
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagOperationsTest {
    private lateinit var database: BudgetDatabase
    private lateinit var operations: TagOperations

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BudgetDatabase::class.java,
        ).build()
        operations = TagOperations(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun createsAndReusesNormalizedTag() = runBlocking {
        val first = operations.createOrReuse("  Mercado   Semanal ")
        val second = operations.createOrReuse("mercado semanal")

        assertEquals(first.id, second.id)
        assertEquals("Mercado Semanal", first.name)
        assertEquals(1, database.tagDao().count())
    }

    @Test
    fun renamesTagAndRejectsNormalizedCollision() = runBlocking {
        val first = operations.createOrReuse("Casa")
        operations.createOrReuse("Mercado")
        val renamed = operations.rename(first.id, "  Lar ")

        assertEquals("Lar", renamed.name)
        assertEquals("lar", renamed.normalizedName)
        assertEquals(first.id, database.tagDao().findById(first.id)?.id)
        try {
            operations.rename(first.id, " MERCADO ")
            throw AssertionError("Normalized collision should be rejected")
        } catch (_: DomainError.DuplicateTag) {
            // Expected domain collision.
        }
    }

    @Test
    fun deletesTagAssociationsWithoutDeletingExpense() = runBlocking {
        val expense = ExpenseEntity("expense-tag", "Compra", 500L, "2026-10-27", 1L, 1L)
        val tag = operations.createOrReuse("Casa")
        database.expenseDao().insert(expense)
        operations.associate(expense.id, tag.id)

        operations.delete(tag.id)

        assertNotNull(database.expenseDao().findById(expense.id))
        assertEquals(0, database.expenseTagDao().countForExpense(expense.id))
        assertNull(database.tagDao().findById(tag.id))
    }
}
