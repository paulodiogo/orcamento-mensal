package br.com.paulodiogo.orcamento.domain

import br.com.paulodiogo.orcamento.data.ExpenseEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseFiltersTest {
    private val expenses = listOf(
        ExpenseEntity("1", "Mercado", 100L, "2026-10-05", 1L, 1L),
        ExpenseEntity("2", "Farmácia", 200L, "2026-11-05", 1L, 1L),
    )

    @Test
    fun filtersByMonthAndDateRange() {
        val tags = mapOf("1" to setOf("food"))
        assertEquals(listOf("1"), filterExpenses(expenses, month = "2026-10", tagIdsByExpense = tags).map { it.id })
        assertEquals(listOf("2"), filterExpenses(expenses, startDate = "2026-11-01", endDate = "2026-11-30", tagIdsByExpense = tags).map { it.id })
    }
}
