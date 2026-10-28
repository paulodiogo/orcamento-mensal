package br.com.paulodiogo.orcamento.domain

import br.com.paulodiogo.orcamento.data.TagBudgetUsageRow
import java.math.BigDecimal
import java.math.RoundingMode

enum class TagBudgetStatus {
    NO_BUDGET,
    ZERO_BUDGET_NO_SPEND,
    ZERO_BUDGET_OVER,
    WITHIN_BUDGET,
    OVER_BUDGET,
}

data class TagBudgetExecution(
    val tagId: String,
    val tagName: String,
    val spentCents: Long,
    val budgetCents: Long?,
    val percentage: BigDecimal?,
    val isOverBudget: Boolean,
    val status: TagBudgetStatus,
) {
    val progressFraction: Float?
        get() = budgetCents
            ?.takeIf { it > 0L }
            ?.let {
                if (spentCents >= it) {
                    1f
                } else {
                    BigDecimal.valueOf(spentCents)
                        .divide(BigDecimal.valueOf(it), 8, RoundingMode.HALF_UP)
                        .toFloat()
                        .coerceAtMost(0.999f)
                }
            }
}

fun calculateTagBudgetExecution(
    tagId: String,
    tagName: String,
    budgetCents: Long?,
    spentCents: Long,
    percentageScale: Int = 2,
): TagBudgetExecution {
    if (budgetCents != null) validateBudgetAmount(budgetCents)
    if (spentCents < 0L) throw DomainError.InvalidSpentAmount
    if (percentageScale < 0) throw IllegalArgumentException("Percentage scale cannot be negative")

    val isOverBudget = budgetCents != null && spentCents > budgetCents
    val percentage = budgetCents
        ?.takeIf { it > 0L }
        ?.let {
            BigDecimal.valueOf(spentCents)
                .multiply(BigDecimal.valueOf(100L))
                .divide(BigDecimal.valueOf(it), percentageScale, RoundingMode.HALF_UP)
        }
    val status = when {
        budgetCents == null -> TagBudgetStatus.NO_BUDGET
        budgetCents == 0L && spentCents == 0L -> TagBudgetStatus.ZERO_BUDGET_NO_SPEND
        budgetCents == 0L -> TagBudgetStatus.ZERO_BUDGET_OVER
        isOverBudget -> TagBudgetStatus.OVER_BUDGET
        else -> TagBudgetStatus.WITHIN_BUDGET
    }

    return TagBudgetExecution(tagId, tagName, spentCents, budgetCents, percentage, isOverBudget, status)
}

fun calculateTagBudgetExecutions(rows: List<TagBudgetUsageRow>): List<TagBudgetExecution> =
    rows.map { row ->
        calculateTagBudgetExecution(
            tagId = row.tagId,
            tagName = row.tagName,
            budgetCents = row.budgetAmountCents,
            spentCents = row.spentCents,
        )
    }.sortedWith(
        compareByDescending<TagBudgetExecution> { it.isOverBudget }
            .thenByDescending { it.percentage ?: BigDecimal.valueOf(-1L) }
            .thenBy { it.tagName.lowercase() },
    )
