package br.com.paulodiogo.orcamento.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class TagTotal(
    val normalizedName: String,
    val amountCents: Long,
)

data class MonthlyTotals(
    val totalSpentCents: Long,
    val availableCents: Long,
)

data class BudgetExecution(
    val spentCents: Long,
    val budgetCents: Long,
    val availableCents: Long,
    val percentage: BigDecimal?,
)

fun calculateBudgetExecution(
    budgetCents: Long,
    spentCents: Long,
    percentageScale: Int = 2,
): BudgetExecution {
    validateBudgetAmount(budgetCents)
    if (spentCents < 0L) throw DomainError.InvalidSpentAmount
    if (percentageScale < 0) throw IllegalArgumentException("Percentage scale cannot be negative")
    val availableCents = try {
        Math.subtractExact(budgetCents, spentCents)
    } catch (_: ArithmeticException) {
        throw DomainError.ArithmeticOverflow
    }
    val percentage = if (budgetCents == 0L) {
        null
    } else {
        BigDecimal.valueOf(spentCents)
            .multiply(BigDecimal.valueOf(100L))
            .divide(BigDecimal.valueOf(budgetCents), percentageScale, RoundingMode.HALF_UP)
    }
    return BudgetExecution(spentCents, budgetCents, availableCents, percentage)
}

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
