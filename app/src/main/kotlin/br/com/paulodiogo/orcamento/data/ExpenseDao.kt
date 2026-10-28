package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun findById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expense ORDER BY occurredOn DESC, id DESC")
    suspend fun findAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expense ORDER BY occurredOn DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT expense.id, expense.description, expense.amountCents, expense.occurredOn, expense.createdAt, expense.updatedAt, tag.id AS tagId, tag.name AS tagName " +
        "FROM expense " +
        "LEFT JOIN expense_tag ON expense_tag.expenseId = expense.id " +
        "LEFT JOIN tag ON tag.id = expense_tag.tagId " +
        "ORDER BY expense.occurredOn DESC, expense.id DESC, tag.normalizedName")
    fun observeAllWithTags(): Flow<List<ExpenseTagListRow>>

    @Query("SELECT expense.id, expense.description, expense.amountCents, expense.occurredOn, expense.createdAt, expense.updatedAt, tag.id AS tagId, tag.name AS tagName " +
        "FROM expense " +
        "LEFT JOIN expense_tag ON expense_tag.expenseId = expense.id " +
        "LEFT JOIN tag ON tag.id = expense_tag.tagId " +
        "ORDER BY expense.occurredOn DESC, expense.id DESC, tag.normalizedName")
    suspend fun findAllWithTags(): List<ExpenseTagListRow>

    @Query("SELECT * FROM expense WHERE occurredOn BETWEEN :startDate AND :endDate ORDER BY occurredOn DESC, id DESC")
    suspend fun findBetween(startDate: String, endDate: String): List<ExpenseEntity>

    @Query("SELECT * FROM expense WHERE description LIKE '%' || :query || '%' ORDER BY occurredOn DESC, id DESC")
    suspend fun search(query: String): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM expense WHERE occurredOn BETWEEN :startDate AND :endDate")
    suspend fun sumAmountBetween(startDate: String, endDate: String): Long

    @Query("SELECT COUNT(*) FROM expense WHERE occurredOn BETWEEN :startDate AND :endDate")
    suspend fun countBetween(startDate: String, endDate: String): Int

    @Query("SELECT substr(occurredOn, 1, 7) AS month, COALESCE(SUM(amountCents), 0) AS amountCents " +
        "FROM expense WHERE occurredOn BETWEEN :startDate AND :endDate " +
        "GROUP BY month ORDER BY month")
    suspend fun sumAmountByMonthBetween(startDate: String, endDate: String): List<MonthlyExpenseTotal>

    @Query("SELECT tag.id AS tagId, tag.name AS tagName, COALESCE(SUM(expense.amountCents), 0) AS amountCents " +
        "FROM expense " +
        "INNER JOIN expense_tag ON expense_tag.expenseId = expense.id " +
        "INNER JOIN tag ON tag.id = expense_tag.tagId " +
        "WHERE expense.occurredOn BETWEEN :startDate AND :endDate " +
        "GROUP BY tag.id, tag.name ORDER BY tag.name")
    suspend fun sumAmountByTagBetween(startDate: String, endDate: String): List<TagExpenseTotal>

    @Query("SELECT tag.id AS tagId, tag.name AS tagName, tag.budget_amount_cents AS budgetAmountCents, " +
        "COALESCE(SUM(CASE WHEN expense.occurredOn BETWEEN :startDate AND :endDate THEN expense.amountCents ELSE 0 END), 0) AS spentCents " +
        "FROM tag " +
        "LEFT JOIN expense_tag ON expense_tag.tagId = tag.id " +
        "LEFT JOIN expense ON expense.id = expense_tag.expenseId " +
        "GROUP BY tag.id, tag.name, tag.budget_amount_cents " +
        "HAVING tag.budget_amount_cents IS NOT NULL OR spentCents > 0 " +
        "ORDER BY tag.name")
    fun observeTagBudgetUsageBetween(startDate: String, endDate: String): Flow<List<TagBudgetUsageRow>>

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expense WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT COUNT(*) FROM expense")
    suspend fun count(): Int
}
