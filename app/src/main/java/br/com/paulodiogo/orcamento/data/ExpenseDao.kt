package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun findById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expense ORDER BY occurredOn DESC, id DESC")
    suspend fun findAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expense WHERE occurredOn BETWEEN :startDate AND :endDate ORDER BY occurredOn DESC, id DESC")
    suspend fun findBetween(startDate: String, endDate: String): List<ExpenseEntity>

    @Query("SELECT * FROM expense WHERE description LIKE '%' || :query || '%' ORDER BY occurredOn DESC, id DESC")
    suspend fun search(query: String): List<ExpenseEntity>

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expense WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT COUNT(*) FROM expense")
    suspend fun count(): Int
}
