package br.com.paulodiogo.orcamento.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budget")
data class BudgetEntity(
    @PrimaryKey
    val month: String,
    val amountCents: Long,
    val createdAt: Long,
    val updatedAt: Long,
)
