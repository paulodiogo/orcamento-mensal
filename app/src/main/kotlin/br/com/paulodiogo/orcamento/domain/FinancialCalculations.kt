package br.com.paulodiogo.orcamento.domain

data class TagTotal(
    val normalizedName: String,
    val amountCents: Long,
)

data class MonthlyTotals(
    val totalSpentCents: Long,
    val availableCents: Long,
)

fun calculateMonthlyTotals(budgetCents: Long, expenseAmountsCents: Iterable<Long>): MonthlyTotals {
    validateBudgetAmount(budgetCents)
    val total = expenseAmountsCents.fold(0L) { sum, amount ->
        validateExpenseAmount(amount)
        sum.checkedPlus(amount)
    }
    return MonthlyTotals(total, budgetCents.checkedMinus(total))
}

fun calculateTagTotals(expenses: Iterable<Pair<Long, Set<String>>>): List<TagTotal> =
    expenses
        .flatMap { (amount, tags) ->
            validateExpenseAmount(amount)
            tags.map { normalizeTagName(it) to amount }
        }
        .groupBy { it.first }
        .map { (tag, values) -> TagTotal(tag, values.sumOfChecked { it.second }) }
        .sortedBy { it.normalizedName }

private fun Long.checkedPlus(other: Long): Long =
    try {
        Math.addExact(this, other).takeUnless { it < 0L } ?: throw DomainError.ArithmeticOverflow
    } catch (_: ArithmeticException) {
        throw DomainError.ArithmeticOverflow
    }

private fun Long.checkedMinus(other: Long): Long =
    try {
        Math.subtractExact(this, other)
    } catch (_: ArithmeticException) {
        throw DomainError.ArithmeticOverflow
    }

private inline fun <T> Iterable<T>.sumOfChecked(selector: (T) -> Long): Long =
    fold(0L) { sum, value -> sum.checkedPlus(selector(value)) }
