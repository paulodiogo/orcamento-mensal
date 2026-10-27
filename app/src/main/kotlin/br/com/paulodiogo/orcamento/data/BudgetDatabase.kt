package br.com.paulodiogo.orcamento.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction

@Database(
    entities = [BudgetEntity::class, ExpenseEntity::class, TagEntity::class, ExpenseTagCrossRef::class],
    version = 1,
    exportSchema = false,
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun tagDao(): TagDao
    abstract fun expenseTagDao(): ExpenseTagDao

    companion object {
        @Volatile
        private var instance: BudgetDatabase? = null

        fun getInstance(context: Context): BudgetDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget.db",
                ).build().also { instance = it }
            }
    }
}
