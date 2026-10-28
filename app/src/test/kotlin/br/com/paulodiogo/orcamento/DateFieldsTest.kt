package br.com.paulodiogo.orcamento

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateFieldsTest {
    @Test
    fun formatsPersistedIsoDateForBrazilianDisplay() {
        assertEquals("27/10/2026", formatDisplayDate("2026-10-27"))
    }

    @Test
    fun invalidOrEmptyDateDoesNotCreateAValue() {
        assertEquals("", formatDisplayDate(""))
        assertEquals("", formatExpenseHistoryDate(""))
        assertNull(parseIsoDate("2026-02-30"))
    }

    @Test
    fun formatsExpenseHistoryDateWithoutYear() {
        assertEquals("15 out", formatExpenseHistoryDate("2026-10-15"))
    }

    @Test
    fun dateConversionDoesNotDependOnLocalTimezone() {
        val value = "2026-12-31"
        assertEquals(value, parseIsoDate(value)?.toString())
    }
}
