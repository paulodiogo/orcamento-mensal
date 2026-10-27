package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(budget: BudgetEntity)

    @Query("SELECT * FROM monthly_budget WHERE month = :month")
    suspend fun findByMonth(month: String): BudgetEntity?

    @Query("SELECT COUNT(*) FROM monthly_budget")
    suspend fun count(): Int
}
