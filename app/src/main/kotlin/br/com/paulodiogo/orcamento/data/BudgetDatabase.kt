package br.com.paulodiogo.orcamento.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BudgetEntity::class, ExpenseEntity::class, TagEntity::class, ExpenseTagCrossRef::class],
    version = 2,
    exportSchema = false,
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun tagDao(): TagDao
    abstract fun expenseTagDao(): ExpenseTagDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tag ADD COLUMN budget_amount_cents INTEGER")
            }
        }

        @Volatile
        private var instance: BudgetDatabase? = null

        fun getInstance(context: Context): BudgetDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
    }
}
