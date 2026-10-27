package br.com.paulodiogo.orcamento.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert
    suspend fun insert(tag: TagEntity)

    @Query("SELECT * FROM tag WHERE id = :id")
    suspend fun findById(id: String): TagEntity?

    @Query("SELECT * FROM tag WHERE normalizedName = :normalizedName")
    suspend fun findByNormalizedName(normalizedName: String): TagEntity?

    @Query("SELECT * FROM tag ORDER BY normalizedName")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag ORDER BY normalizedName")
    suspend fun findAll(): List<TagEntity>

    @Update
    suspend fun update(tag: TagEntity)

    @Query("DELETE FROM tag WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM expense_tag WHERE tagId = :tagId")
    suspend fun deleteAssociations(tagId: String)

    @Query("SELECT COUNT(*) FROM tag")
    suspend fun count(): Int
}
