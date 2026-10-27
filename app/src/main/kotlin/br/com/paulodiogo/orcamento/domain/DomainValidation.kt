package br.com.paulodiogo.orcamento.domain

sealed class DomainError(message: String) : IllegalArgumentException(message) {
    data object InvalidAmount : DomainError("Amount must be greater than zero")
    data object NegativeBudget : DomainError("Budget cannot be negative")
    data object EmptyDescription : DomainError("Description cannot be empty")
    data object EmptyTagName : DomainError("Tag name cannot be empty")
    data object InvalidDate : DomainError("Occurrence date must use yyyy-MM-dd")
    data object ArithmeticOverflow : DomainError("Financial calculation overflow")
    data object DuplicateTag : DomainError("Tag already exists")
}

fun validateExpenseAmount(amountCents: Long) {
    if (amountCents <= 0L) throw DomainError.InvalidAmount
}

fun validateBudgetAmount(amountCents: Long) {
    if (amountCents < 0L) throw DomainError.NegativeBudget
}

fun validateDescription(description: String): String =
    description.trim().also {
        if (it.isEmpty()) throw DomainError.EmptyDescription
    }

fun validateOccurredOn(occurredOn: String): String =
    occurredOn.trim().also {
        if (!DATE_PATTERN.matches(it)) throw DomainError.InvalidDate
    }

private val DATE_PATTERN = Regex("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])")
