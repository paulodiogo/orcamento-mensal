package br.com.paulodiogo.orcamento.domain

import br.com.paulodiogo.orcamento.data.ExpenseEntity

fun validateDateRange(startDate: String?, endDate: String?) {
    startDate?.let(::validateOccurredOn)
    endDate?.let(::validateOccurredOn)
    if (startDate != null && endDate != null && startDate > endDate) {
        throw DomainError.InvalidDateRange
    }
}

fun filterExpenses(
    expenses: List<ExpenseEntity>,
    query: String = "",
    month: String? = null,
    startDate: String? = null,
    endDate: String? = null,
    tagIdsByExpense: Map<String, Set<String>> = emptyMap(),
    tagId: String? = null,
    withoutTag: Boolean = false,
): List<ExpenseEntity> = expenses.filter { expense ->
    val matchesQuery = query.isBlank() || expense.description.contains(query.trim(), ignoreCase = true)
    val matchesMonth = month == null || expense.occurredOn.startsWith(month)
    val matchesRange = (startDate == null || expense.occurredOn >= startDate) &&
        (endDate == null || expense.occurredOn <= endDate)
    val tags = tagIdsByExpense[expense.id].orEmpty()
    val matchesTag = tagId == null || tagId in tags
    val matchesWithoutTag = !withoutTag || tags.isEmpty()
    matchesQuery && matchesMonth && matchesRange && matchesTag && matchesWithoutTag
}
