package br.com.paulodiogo.orcamento.domain

const val MAX_MONEY_DIGITS = 19

enum class MoneyInputFailure {
    EMPTY,
    NON_DIGIT,
    TOO_MANY_DIGITS,
    OUT_OF_RANGE,
    NEGATIVE,
}

class InvalidMoneyInput(val reason: MoneyInputFailure) : IllegalArgumentException()

fun sanitizeMoneyDigits(input: String): String = input.filter { it in '0'..'9' }

fun normalizeMoneyDigits(digits: String): String {
    require(digits.all { it in '0'..'9' }) { "Money input must contain only digits" }
    return digits.trimStart('0').ifEmpty { if (digits.isEmpty()) "" else "0" }
}

fun parseMoneyDigitsToCents(digits: String): Long {
    if (digits.isEmpty() || digits.any { it !in '0'..'9' }) {
        throw InvalidMoneyInput(if (digits.isEmpty()) MoneyInputFailure.EMPTY else MoneyInputFailure.NON_DIGIT)
    }
    if (digits.length > MAX_MONEY_DIGITS) {
        throw InvalidMoneyInput(MoneyInputFailure.TOO_MANY_DIGITS)
    }
    return digits.toLongOrNull() ?: throw InvalidMoneyInput(MoneyInputFailure.OUT_OF_RANGE)
}

fun formatMoneyDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    parseMoneyDigitsToCents(digits)
    val normalized = normalizeMoneyDigits(digits)
    val whole = if (normalized.length > 2) normalized.dropLast(2) else "0"
    val fraction = normalized.takeLast(2).padStart(2, '0')
    return "${formatThousands(whole)},$fraction"
}

fun moneyDigitsFromCents(cents: Long): String {
    if (cents < 0L) throw InvalidMoneyInput(MoneyInputFailure.NEGATIVE)
    return cents.toString()
}

fun formatCentsAsMoney(cents: Long): String = formatMoneyDigits(moneyDigitsFromCents(cents))

private fun formatThousands(value: String): String =
    value.reversed().chunked(3).joinToString(".").reversed()
