package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseTagDao {
    @Insert
    suspend fun insert(association: ExpenseTagCrossRef)

    @Query("SELECT COUNT(*) FROM expense_tag WHERE expenseId = :expenseId")
    suspend fun countForExpense(expenseId: String): Int

    @Query("SELECT COUNT(*) FROM expense_tag WHERE tagId = :tagId")
    suspend fun countForTag(tagId: String): Int
}
