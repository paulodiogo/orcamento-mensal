package br.com.paulodiogo.orcamento.domain

import br.com.paulodiogo.orcamento.data.TagBudgetUsageRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class TagBudgetExecutionTest {
    @Test
    fun classifiesAllBudgetStatesAndProgressBoundaries() {
        assertEquals(TagBudgetStatus.NO_BUDGET, calculateTagBudgetExecution("a", "A", null, 100L).status)
        assertEquals(TagBudgetStatus.ZERO_BUDGET_NO_SPEND, calculateTagBudgetExecution("b", "B", 0L, 0L).status)
        assertEquals(TagBudgetStatus.ZERO_BUDGET_OVER, calculateTagBudgetExecution("c", "C", 0L, 1L).status)
        assertEquals(TagBudgetStatus.WITHIN_BUDGET, calculateTagBudgetExecution("d", "D", 100L, 50L).status)
        assertEquals(TagBudgetStatus.OVER_BUDGET, calculateTagBudgetExecution("e", "E", 100L, 101L).status)
        assertNull(calculateTagBudgetExecution("a", "A", null, 100L).progressFraction)
        assertEquals(0f, calculateTagBudgetExecution("b", "B", 100L, 0L).progressFraction)
        assertEquals(0.5f, calculateTagBudgetExecution("d", "D", 100L, 50L).progressFraction)
    }

    @Test
    fun sortsOverBudgetThenPercentageThenName() {
        val rows = listOf(
            TagBudgetUsageRow("no", "No", null, 1L),
            TagBudgetUsageRow("within", "Within", 100L, 50L),
            TagBudgetUsageRow("over-low", "Over low", 100L, 101L),
            TagBudgetUsageRow("over-high", "Over high", 100L, 200L),
        )
        assertEquals(listOf("over-high", "over-low", "within", "no"), calculateTagBudgetExecutions(rows).map { it.tagId })
        assertEquals(BigDecimal("200.00"), calculateTagBudgetExecutions(rows).first().percentage)
    }

    @Test
    fun rejectsInvalidScaleAndSpentAmount() {
        assertThrows(DomainError.InvalidSpentAmount::class.java) { calculateTagBudgetExecution("a", "A", null, -1L) }
        assertThrows(IllegalArgumentException::class.java) { calculateTagBudgetExecution("a", "A", 1L, 0L, -1) }
    }
}
