package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun findById(id: String): ExpenseEntity?

    @Query("SELECT COUNT(*) FROM expense")
    suspend fun count(): Int
}
