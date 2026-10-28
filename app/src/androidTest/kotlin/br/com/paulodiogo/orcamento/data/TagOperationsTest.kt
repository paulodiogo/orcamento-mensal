package br.com.paulodiogo.orcamento.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.domain.BudgetOperations
import br.com.paulodiogo.orcamento.domain.DomainError
import br.com.paulodiogo.orcamento.domain.TagOperations
import kotlinx.coroutines.flow.first
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
    fun initializesDefaultTagsIdempotentlyWithoutFinancialRecords() = runBlocking {
        operations.initializeDefaultTags(now = 10L)
        operations.initializeDefaultTags(now = 20L)

        assertEquals(listOf("Alimentação", "Mercado"), database.tagDao().findAll().map { it.name })
        assertEquals(2, database.tagDao().count())
        assertEquals(0, database.budgetDao().count())
        assertEquals(0, database.expenseDao().count())
        assertEquals(0, database.expenseTagDao().count())
        assertEquals(10L, database.tagDao().findByNormalizedName("alimentação")?.createdAt)

        val custom = operations.createOrReuse("Personalizada", budgetAmountCents = 12_000L, now = 30L)
        operations.initializeDefaultTags(now = 40L)
        assertEquals(custom, database.tagDao().findById(custom.id))
        assertEquals(3, database.tagDao().count())
    }

    @Test
    fun initializesOnlyMissingDefaultTagsAndPreservesExistingValues() = runBlocking {
        val food = operations.createOrReuse("Alimentação", budgetAmountCents = 5_000L, now = 1L)
        operations.initializeDefaultTags(now = 2L)

        assertEquals(food, database.tagDao().findById(food.id))
        assertNotNull(database.tagDao().findByNormalizedName("mercado"))
        assertEquals(2, database.tagDao().count())
    }

    @Test
    fun initializesFoodWhenOnlyMarketExists() = runBlocking {
        operations.createOrReuse("Mercado")
        operations.initializeDefaultTags()

        assertNotNull(database.tagDao().findByNormalizedName("alimentação"))
        assertEquals(2, database.tagDao().count())
    }

    @Test
    fun doesNothingWhenBothDefaultTagsExist() = runBlocking {
        val food = operations.createOrReuse("Alimentação")
        val market = operations.createOrReuse("Mercado")
        operations.initializeDefaultTags(now = 2L)

        assertEquals(listOf(food, market), database.tagDao().findAll())
    }

    @Test
    fun createdTagAppearsInReactiveQuery() = runBlocking {
        val created = operations.createOrReuse("Recente")

        assertEquals(listOf(created.id), database.tagDao().observeAll().first().map { it.id })
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
    fun createsEditsAndRemovesOptionalBudgetWithoutChangingAssociations() = runBlocking {
        val tag = operations.createOrReuse("Casa", 12_345L)
        val expense = ExpenseEntity("expense-budget-tag", "Compra", 500L, "2026-10-27", 1L, 1L)
        database.expenseDao().insert(expense)
        operations.associate(expense.id, tag.id)

        assertEquals(12_345L, database.tagDao().findById(tag.id)?.budgetAmountCents)
        operations.update(tag.id, tag.name, 67_890L)
        assertEquals(67_890L, database.tagDao().findById(tag.id)?.budgetAmountCents)
        assertEquals(1, database.expenseTagDao().countForExpense(expense.id))

        operations.update(tag.id, tag.name, null)
        assertNull(database.tagDao().findById(tag.id)?.budgetAmountCents)
        assertEquals(1, database.expenseTagDao().countForExpense(expense.id))
    }

    @Test
    fun reusingExistingTagDoesNotOverwriteItsBudget() = runBlocking {
        val first = operations.createOrReuse("Casa", 10_000L)
        val second = operations.createOrReuse(" casa ", 99_000L)

        assertEquals(first.id, second.id)
        assertEquals(10_000L, database.tagDao().findById(first.id)?.budgetAmountCents)
    }

    @Test
    fun rejectsNegativeTagBudget() = runBlocking {
        try {
            operations.createOrReuse("Inválida", -1L)
            throw AssertionError("Negative tag budget should be rejected")
        } catch (_: DomainError.NegativeBudget) {
            // Expected domain validation.
        }
    }

    @Test
    fun tagBudgetsRemainIndependentFromMonthlyBudget() = runBlocking {
        val monthly = BudgetOperations(database)
        val first = operations.createOrReuse("Independência A", 80_000L)
        val second = operations.createOrReuse("Independência B", 80_000L)
        monthly.save("2026-10", 100_000L, now = 1L)

        assertEquals(100_000L, monthly.findByMonth("2026-10")?.amountCents)
        operations.update(first.id, first.name, 120_000L)
        assertEquals(100_000L, monthly.findByMonth("2026-10")?.amountCents)
        monthly.save("2026-10", 50_000L, now = 2L)
        assertEquals(120_000L, database.tagDao().findById(first.id)?.budgetAmountCents)
        assertEquals(50_000L, monthly.findByMonth("2026-10")?.amountCents)
        assertEquals(200_000L, (database.tagDao().findById(first.id)?.budgetAmountCents ?: 0L) + (database.tagDao().findById(second.id)?.budgetAmountCents ?: 0L))

        operations.update(first.id, first.name, null)
        assertNull(database.tagDao().findById(first.id)?.budgetAmountCents)
        assertEquals(50_000L, monthly.findByMonth("2026-10")?.amountCents)
        val withoutBudget = operations.createOrReuse("Sem orçamento")
        assertNull(withoutBudget.budgetAmountCents)
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
