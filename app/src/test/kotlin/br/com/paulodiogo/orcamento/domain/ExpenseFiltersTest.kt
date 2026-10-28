package br.com.paulodiogo.orcamento.domain

import br.com.paulodiogo.orcamento.data.ExpenseEntity
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class ExpenseFiltersTest {
    @Test
    fun rejectsReversedDateRange() {
        org.junit.Assert.assertThrows(DomainError.InvalidDateRange::class.java) {
            validateDateRange("2026-10-31", "2026-10-01")
        }
    }

    @Test
    fun acceptsInclusiveDateRangeBoundaries() {
        validateDateRange("2026-10-01", "2026-10-31")
    }

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

    @Test
    fun filtersByDescriptionTagAndExpensesWithoutTags() {
        val moreExpenses = expenses + ExpenseEntity("3", "Mercado", 300L, "2026-10-20", 1L, 1L)
        val tags = mapOf("1" to setOf("food", "home"), "2" to setOf("medicine"))

        assertEquals(listOf("1", "3"), filterExpenses(moreExpenses, query = " mercado ", tagIdsByExpense = tags).map { it.id })
        assertEquals(listOf("1"), filterExpenses(moreExpenses, tagId = "home", tagIdsByExpense = tags).map { it.id })
        assertEquals(listOf("3"), filterExpenses(moreExpenses, withoutTag = true, tagIdsByExpense = tags).map { it.id })
        assertEquals(emptyList<String>(), filterExpenses(moreExpenses, tagId = "missing", tagIdsByExpense = tags).map { it.id })
    }
}

class FinancialCalculationsTest {
    @Test
    fun calculatesPercentageWithDeterministicRounding() {
        val result = calculateBudgetExecution(budgetCents = 3L, spentCents = 1L)

        assertEquals(BigDecimal("33.33"), result.percentage)
    }

    @Test
    fun returnsUnavailablePercentageForZeroBudget() {
        val result = calculateBudgetExecution(budgetCents = 0L, spentCents = 0L)

        Assert.assertNull(result.percentage)
    }

    @Test
    fun returnsNegativeBalanceWhenSpendingExceedsBudget() {
        val result = calculateBudgetExecution(budgetCents = 1_000L, spentCents = 1_250L)

        assertEquals(-250L, result.availableCents)
    }

    @Test
    fun roundsHalfUpToConfiguredScale() {
        val result = calculateBudgetExecution(
            budgetCents = 8L,
            spentCents = 1L,
            percentageScale = 1,
        )

        assertEquals(BigDecimal("12.5"), result.percentage)
    }
}