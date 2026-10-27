package br.com.paulodiogo.orcamento.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class DomainRulesTest {
    @Test
    fun calculatesReferenceScenarioWithoutDuplicatingMultitagExpense() {
        val totals = calculateMonthlyTotals(100_000L, listOf(30_000L, 15_000L))
        val byTag = calculateTagTotals(
            listOf(
                30_000L to setOf("Alimentação", "Casa"),
                15_000L to setOf("Transporte"),
            ),
        )
        assertEquals(45_000L, totals.totalSpentCents)
        assertEquals(55_000L, totals.availableCents)
        assertEquals(listOf(TagTotal("alimentação", 30_000L), TagTotal("casa", 30_000L), TagTotal("transporte", 15_000L)), byTag)
    }

    @Test
    fun calculatesBudgetExecutionWithDeterministicPercentage() {
        assertEquals(
            BigDecimal("25.00"),
            calculateBudgetExecution(10_000L, 2_500L).percentage,
        )
        assertEquals(
            BigDecimal("100.00"),
            calculateBudgetExecution(10_000L, 10_000L).percentage,
        )
        assertEquals(
            BigDecimal("150.00"),
            calculateBudgetExecution(10_000L, 15_000L).percentage,
        )
        assertEquals(-5_000L, calculateBudgetExecution(10_000L, 15_000L).availableCents)
    }

    @Test
    fun handlesZeroBudgetZeroSpendAndRoundedValues() {
        assertEquals(null, calculateBudgetExecution(0L, 0L).percentage)
        assertEquals(BigDecimal("33.33"), calculateBudgetExecution(3L, 1L).percentage)
        assertEquals(BigDecimal("33.333"), calculateBudgetExecution(3L, 1L, 3).percentage)
    }

    @Test
    fun rejectsInvalidSpentAmountAndOverflow() {
        assertThrows(DomainError.InvalidSpentAmount::class.java) { calculateBudgetExecution(10L, -1L) }
        assertThrows(DomainError.ArithmeticOverflow::class.java) {
            calculateMonthlyTotals(Long.MAX_VALUE, listOf(Long.MAX_VALUE, Long.MAX_VALUE))
        }
    }

    @Test
    fun allowsZeroBudgetAndNegativeAvailableBalance() {
        assertEquals(-1L, calculateMonthlyTotals(0L, listOf(1L)).availableCents)
    }

    @Test
    fun rejectsInvalidAmountsAndDescriptions() {
        assertThrows(DomainError.NegativeBudget::class.java) { validateBudgetAmount(-1L) }
        assertThrows(DomainError.InvalidAmount::class.java) { validateExpenseAmount(0L) }
        assertThrows(DomainError.EmptyDescription::class.java) { validateDescription("  ") }
    }

    @Test
    fun normalizesWhitespaceAndCase() {
        assertEquals("mercado semanal", normalizeTagName("  Mercado   Semanal "))
        assertEquals("Mercado Semanal", displayTagName("  Mercado   Semanal "))
        assertEquals(normalizeTagName("MERCADO"), normalizeTagName("mercado"))
    }

    @Test
    fun rejectsInvalidDatesAndDetectsOverflow() {
        assertThrows(DomainError.InvalidDate::class.java) { validateOccurredOn("2026-13-01") }
        assertThrows(DomainError.ArithmeticOverflow::class.java) { calculateMonthlyTotals(Long.MAX_VALUE, listOf(Long.MAX_VALUE, Long.MAX_VALUE)) }
    }
}
