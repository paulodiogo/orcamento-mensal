package br.com.paulodiogo.orcamento.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense",
    indices = [Index(value = ["occurredOn"])],
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val description: String,
    val amountCents: Long,
    val occurredOn: String,
    val createdAt: Long,
    val updatedAt: Long,
)
