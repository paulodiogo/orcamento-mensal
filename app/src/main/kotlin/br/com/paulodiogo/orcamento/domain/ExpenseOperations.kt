package br.com.paulodiogo.orcamento.domain

import androidx.room.withTransaction
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.ExpenseEntity
import br.com.paulodiogo.orcamento.data.ExpenseTagCrossRef
import java.util.UUID

class ExpenseOperations(private val database: BudgetDatabase) {
    suspend fun create(
        description: String,
        amountCents: Long,
        occurredOn: String,
        tagIds: List<String> = emptyList(),
        id: String = UUID.randomUUID().toString(),
        now: Long = System.currentTimeMillis(),
    ): ExpenseEntity {
        val expense = ExpenseEntity(
            id = id,
            description = validateDescription(description),
            amountCents = amountCents.also(::validateExpenseAmount),
            occurredOn = validateOccurredOn(occurredOn),
            createdAt = now,
            updatedAt = now,
        )
        database.withTransaction {
            database.expenseDao().insert(expense)
            validateTagIds(tagIds)
            database.expenseTagDao().insertAll(tagIds.distinct().map { ExpenseTagCrossRef(id, it) })
        }
        return expense
    }

    suspend fun findById(id: String): ExpenseEntity? = database.expenseDao().findById(id)

    suspend fun findAll(): List<ExpenseEntity> = database.expenseDao().findAll()

    suspend fun findByIdWithTags(id: String): Pair<ExpenseEntity, List<String>>? =
        database.expenseDao().findById(id)?.let { it to findTagIds(id) }

    suspend fun findTagIds(expenseId: String): List<String> =
        database.expenseTagDao().findTagIdsForExpense(expenseId)

    suspend fun update(
        expense: ExpenseEntity,
        description: String = expense.description,
        amountCents: Long = expense.amountCents,
        occurredOn: String = expense.occurredOn,
        now: Long = System.currentTimeMillis(),
    ): ExpenseEntity {
        val updated = expense.copy(
            description = validateDescription(description),
            amountCents = amountCents.also(::validateExpenseAmount),
            occurredOn = validateOccurredOn(occurredOn),
            updatedAt = now,
        )
        database.expenseDao().update(updated)
        return updated
    }

    suspend fun updateTags(expenseId: String, tagIds: List<String>) = database.withTransaction {
        if (database.expenseDao().findById(expenseId) == null) error("Expense not found")
        validateTagIds(tagIds)
        database.expenseTagDao().deleteForExpense(expenseId)
        database.expenseTagDao().insertAll(tagIds.distinct().map { ExpenseTagCrossRef(expenseId, it) })
    }

    private suspend fun validateTagIds(tagIds: List<String>) {
        tagIds.forEach { tagId ->
            if (database.tagDao().findById(tagId) == null) error("Tag not found")
        }
    }

    suspend fun delete(id: String): Boolean = database.withTransaction {
        database.expenseTagDao().deleteForExpense(id)
        database.expenseDao().deleteById(id) > 0
    }
}
