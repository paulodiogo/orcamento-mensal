package br.com.paulodiogo.orcamento.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag",
    indices = [Index(value = ["normalizedName"], unique = true)],
)
data class TagEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val normalizedName: String,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(name = "budget_amount_cents")
    val budgetAmountCents: Long? = null,
)
