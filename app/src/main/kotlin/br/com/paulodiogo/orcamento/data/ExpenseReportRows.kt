package br.com.paulodiogo.orcamento.data

data class MonthlyExpenseTotal(
    val month: String,
    val amountCents: Long,
)

data class TagExpenseTotal(
    val tagId: String,
    val tagName: String,
    val amountCents: Long,
)
