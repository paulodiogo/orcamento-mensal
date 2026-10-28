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

data class TagBudgetUsageRow(
    val tagId: String,
    val tagName: String,
    val budgetAmountCents: Long?,
    val spentCents: Long,
)

data class ExpenseTagListRow(
    val id: String,
    val description: String,
    val amountCents: Long,
    val occurredOn: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tagId: String?,
    val tagName: String?,
)
