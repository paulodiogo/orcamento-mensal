package br.com.paulodiogo.orcamento.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyFormattingTest {
    @Test
    fun formatsImplicitCentsAndThousands() {
        assertEquals("0,01", formatMoneyDigits("1"))
        assertEquals("0,12", formatMoneyDigits("12"))
        assertEquals("1,23", formatMoneyDigits("123"))
        assertEquals("12,34", formatMoneyDigits("1234"))
        assertEquals("123,45", formatMoneyDigits("12345"))
        assertEquals("1.234,56", formatMoneyDigits("123456"))
    }

    @Test
    fun parsesDigitsAsCentsAndNormalizesLeadingZeroes() {
        assertEquals(12_345L, parseMoneyDigitsToCents("12345"))
        assertEquals("0,01", formatMoneyDigits("0001"))
        assertEquals("0,00", formatMoneyDigits("000"))
    }

    @Test
    fun preservesEmptyAndFormatsPersistedCents() {
        assertEquals("", formatMoneyDigits(""))
        assertEquals("1.234,56", formatCentsAsMoney(123_456L))
        assertEquals("0,00", formatCentsAsMoney(0L))
    }

    @Test
    fun sanitizesClipboardTextToAsciiDigits() {
        assertEquals("123456", sanitizeMoneyDigits("R$ 1.234,56"))
        assertEquals("", sanitizeMoneyDigits("R$ -., "))
        assertEquals("", sanitizeMoneyDigits("１２３"))
    }

    @Test
    fun acceptsLongMaximumAndRejectsOverflowWithoutTruncation() {
        assertEquals(Long.MAX_VALUE, parseMoneyDigitsToCents(Long.MAX_VALUE.toString()))
        assertEquals("92.233.720.368.547.758,07", formatCentsAsMoney(Long.MAX_VALUE))
        assertThrows(InvalidMoneyInput::class.java) {
            parseMoneyDigitsToCents("9223372036854775808")
        }
        assertThrows(InvalidMoneyInput::class.java) {
            parseMoneyDigitsToCents("123456789012345678901")
        }
    }

    @Test
    fun rejectsEmptyAndNonDigitDomainInputs() {
        assertThrows(InvalidMoneyInput::class.java) { parseMoneyDigitsToCents("") }
        assertThrows(InvalidMoneyInput::class.java) { parseMoneyDigitsToCents("12,34") }
        assertThrows(InvalidMoneyInput::class.java) { parseMoneyDigitsToCents("-1") }
    }

    @Test
    fun roundTripsCentsWithinLongRange() {
        listOf(0L, 1L, 12L, 123L, 123_456L, Long.MAX_VALUE).forEach { cents ->
            assertEquals(cents, parseMoneyDigitsToCents(moneyDigitsFromCents(cents)))
        }
    }

    @Test
    fun rejectsNegativeCentsWhenFormattingPersistedValues() {
        assertThrows(InvalidMoneyInput::class.java) { formatCentsAsMoney(-1L) }
    }
}
