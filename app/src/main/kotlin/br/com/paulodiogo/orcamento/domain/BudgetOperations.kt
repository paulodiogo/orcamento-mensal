package br.com.paulodiogo.orcamento.domain

import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.DatabaseOperationGate
import br.com.paulodiogo.orcamento.data.BudgetEntity
import java.time.YearMonth

class BudgetOperations(private val database: BudgetDatabase) {
    suspend fun findByMonth(month: String): BudgetEntity? {
        val validMonth = validateMonth(month)
        return database.budgetDao().findByMonth(validMonth)
    }

    suspend fun findAll(): List<BudgetEntity> = database.budgetDao().findAll()

    suspend fun suggestedAmountFor(month: String): Long? {
        val target = YearMonth.parse(validateMonth(month))
        return findAll()
            .asSequence()
            .filter { YearMonth.parse(it.month).isBefore(target) }
            .maxByOrNull { it.month }
            ?.amountCents
    }

    suspend fun save(month: String, amountCents: Long, now: Long = System.currentTimeMillis()): BudgetEntity =
        DatabaseOperationGate.write {
            val validMonth = validateMonth(month)
            validateBudgetAmount(amountCents)
            val existing = database.budgetDao().findByMonth(validMonth)
            val budget = BudgetEntity(validMonth, amountCents, existing?.createdAt ?: now, now)
            database.budgetDao().save(budget)
            budget
        }
}
