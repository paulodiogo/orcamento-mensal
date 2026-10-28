package br.com.paulodiogo.orcamento.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.withTransaction
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun migratesVersionOneDatabaseAndPreservesAllData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration-test.db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS monthly_budget (month TEXT NOT NULL, amountCents INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(month))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS expense (id TEXT NOT NULL, description TEXT NOT NULL, amountCents INTEGER NOT NULL, occurredOn TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_occurredOn ON expense (occurredOn)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS tag (id TEXT NOT NULL, name TEXT NOT NULL, normalizedName TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tag_normalizedName ON tag (normalizedName)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS expense_tag (expenseId TEXT NOT NULL, tagId TEXT NOT NULL, PRIMARY KEY(expenseId, tagId), FOREIGN KEY(expenseId) REFERENCES expense(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(tagId) REFERENCES tag(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_tag_tagId ON expense_tag (tagId)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val oldDatabase = helper.writableDatabase
        oldDatabase.execSQL("INSERT INTO monthly_budget VALUES ('2026-10', 100000, 1, 1)")
        oldDatabase.execSQL("INSERT INTO expense VALUES ('expense-1', 'Compra', 2500, '2026-10-27', 1, 1)")
        oldDatabase.execSQL("INSERT INTO tag VALUES ('tag-1', 'Casa', 'casa', 1, 1)")
        oldDatabase.execSQL("INSERT INTO expense_tag VALUES ('expense-1', 'tag-1')")
        oldDatabase.version = 1
        oldDatabase.close()
        helper.close()

        val migratedDatabase = Room.databaseBuilder(context, BudgetDatabase::class.java, "migration-test.db")
            .addMigrations(BudgetDatabase.MIGRATION_1_2)
            .build()
        val migratedSqlite = migratedDatabase.openHelper.writableDatabase
        val columns = migratedSqlite.query("PRAGMA table_info(tag)").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        assertTrue(columns.contains("budget_amount_cents"))
        assertNull(migratedSqlite.query("SELECT budget_amount_cents FROM tag WHERE id = 'tag-1'").use { cursor -> cursor.moveToFirst(); if (cursor.isNull(0)) null else cursor.getLong(0) })
        assertEquals("Casa", migratedSqlite.query("SELECT name FROM tag WHERE id = 'tag-1'").use { cursor -> cursor.moveToFirst(); cursor.getString(0) })
        assertEquals("Compra", migratedSqlite.query("SELECT description FROM expense WHERE id = 'expense-1'").use { cursor -> cursor.moveToFirst(); cursor.getString(0) })
        assertEquals(100000L, migratedSqlite.query("SELECT amountCents FROM monthly_budget WHERE month = '2026-10'").use { cursor -> cursor.moveToFirst(); cursor.getLong(0) })
        assertEquals(1, migratedSqlite.query("SELECT COUNT(*) FROM expense_tag WHERE expenseId = 'expense-1' AND tagId = 'tag-1'").use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
        val tagIndexes = migratedSqlite.query("PRAGMA index_list('tag')").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        val expenseTagIndexes = migratedSqlite.query("PRAGMA index_list('expense_tag')").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
        }
        assertTrue(tagIndexes.contains("index_tag_normalizedName"))
        assertTrue(expenseTagIndexes.contains("index_expense_tag_tagId"))
        assertNull(runDatabaseOn(migratedDatabase) { tagDao().findById("tag-1")?.budgetAmountCents })
        assertEquals(100000L, runDatabaseOn(migratedDatabase) { budgetDao().findByMonth("2026-10")?.amountCents })
        migratedDatabase.close()
        context.deleteDatabase("migration-test.db")
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
    fun aggregatesExpensesByPeriodMonthAndTagWithoutDuplicatingMultitagTotals() {
        val casa = TagEntity("tag-casa", "Casa", "casa", 1L, 1L)
        val mercado = TagEntity("tag-mercado", "Mercado", "mercado", 1L, 1L)
        val first = ExpenseEntity("expense-1", "Compra", 1_000L, "2026-10-01", 1L, 1L)
        val second = ExpenseEntity("expense-2", "Contas", 2_000L, "2026-10-31", 1L, 1L)
        val outside = ExpenseEntity("expense-3", "Fora", 4_000L, "2026-11-01", 1L, 1L)
        runDatabase {
            tagDao().insert(casa)
            tagDao().insert(mercado)
            expenseDao().insert(first)
            expenseDao().insert(second)
            expenseDao().insert(outside)
            expenseTagDao().insertAll(
                listOf(
                    ExpenseTagCrossRef(first.id, casa.id),
                    ExpenseTagCrossRef(first.id, mercado.id),
                    ExpenseTagCrossRef(second.id, casa.id),
                ),
            )
        }

        assertEquals(3_000L, runDatabase { expenseDao().sumAmountBetween("2026-10-01", "2026-10-31") })
        assertEquals(2, runDatabase { expenseDao().countBetween("2026-10-01", "2026-10-31") })
        assertEquals(
            listOf(
                MonthlyExpenseTotal("2026-10", 3_000L),
            ),
            runDatabase { expenseDao().sumAmountByMonthBetween("2026-10-01", "2026-10-31") },
        )
        assertEquals(
            listOf(
                TagExpenseTotal(casa.id, casa.name, 3_000L),
                TagExpenseTotal(mercado.id, mercado.name, 1_000L),
            ),
            runDatabase { expenseDao().sumAmountByTagBetween("2026-10-01", "2026-10-31") },
        )
        assertEquals(0L, runDatabase { expenseDao().sumAmountBetween("2027-01-01", "2027-01-31") })
        assertEquals(0, runDatabase { expenseDao().countBetween("2027-01-01", "2027-01-31") })
        assertTrue(runDatabase { expenseDao().sumAmountByMonthBetween("2027-01-01", "2027-01-31") }.isEmpty())
        assertTrue(runDatabase { expenseDao().sumAmountByTagBetween("2027-01-01", "2027-01-31") }.isEmpty())
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

    private fun <T> runDatabaseOn(target: BudgetDatabase, block: suspend BudgetDatabase.() -> T): T =
        kotlinx.coroutines.runBlocking { block(target) }
}

