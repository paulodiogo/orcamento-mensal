package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(association: ExpenseTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(associations: List<ExpenseTagCrossRef>)

    @Query("SELECT COUNT(*) FROM expense_tag WHERE expenseId = :expenseId")
    suspend fun countForExpense(expenseId: String): Int

    @Query("SELECT COUNT(*) FROM expense_tag WHERE tagId = :tagId")
    suspend fun countForTag(tagId: String): Int

    @Query("SELECT tagId FROM expense_tag WHERE expenseId = :expenseId")
    suspend fun findTagIdsForExpense(expenseId: String): List<String>

    @Query("DELETE FROM expense_tag WHERE expenseId = :expenseId")
    suspend fun deleteForExpense(expenseId: String)

    @Query("DELETE FROM expense_tag WHERE expenseId = :expenseId AND tagId = :tagId")
    suspend fun delete(expenseId: String, tagId: String)
}
