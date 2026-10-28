package br.com.paulodiogo.orcamento.domain

import androidx.room.withTransaction
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.ExpenseTagCrossRef
import br.com.paulodiogo.orcamento.data.TagEntity
import java.util.UUID

class TagOperations(private val database: BudgetDatabase) {
    suspend fun create(
        name: String,
        budgetAmountCents: Long? = null,
        now: Long = System.currentTimeMillis(),
    ): TagEntity {
        validateOptionalTagBudget(budgetAmountCents)
        val displayName = displayTagName(name)
        val normalizedName = normalizeTagName(displayName)
        return database.withTransaction {
            if (database.tagDao().findByNormalizedName(normalizedName) != null) throw DomainError.DuplicateTag
            TagEntity(UUID.randomUUID().toString(), displayName, normalizedName, now, now, budgetAmountCents)
                .also { database.tagDao().insert(it) }
        }
    }

    suspend fun createOrReuse(
        name: String,
        budgetAmountCents: Long? = null,
        now: Long = System.currentTimeMillis(),
    ): TagEntity {
        validateOptionalTagBudget(budgetAmountCents)
        val displayName = displayTagName(name)
        val normalizedName = normalizeTagName(displayName)
        return database.withTransaction {
            database.tagDao().findByNormalizedName(normalizedName)
                ?: TagEntity(UUID.randomUUID().toString(), displayName, normalizedName, now, now, budgetAmountCents)
                    .also { database.tagDao().insert(it) }
        }
    }

    suspend fun rename(tagId: String, name: String, now: Long = System.currentTimeMillis()): TagEntity =
        update(tagId, name, database.tagDao().findById(tagId)?.budgetAmountCents, now)

    suspend fun update(
        tagId: String,
        name: String,
        budgetAmountCents: Long?,
        now: Long = System.currentTimeMillis(),
    ): TagEntity {
        validateOptionalTagBudget(budgetAmountCents)
        val displayName = displayTagName(name)
        val normalizedName = normalizeTagName(displayName)
        return database.withTransaction {
            val current = database.tagDao().findById(tagId) ?: error("Tag not found")
            val collision = database.tagDao().findByNormalizedName(normalizedName)
            if (collision != null && collision.id != tagId) throw DomainError.DuplicateTag
            current.copy(
                name = displayName,
                normalizedName = normalizedName,
                updatedAt = now,
                budgetAmountCents = budgetAmountCents,
            ).also { database.tagDao().update(it) }
        }
    }

    private fun validateOptionalTagBudget(amountCents: Long?) {
        if (amountCents != null) validateBudgetAmount(amountCents)
    }

    suspend fun delete(tagId: String) {
        database.withTransaction {
            database.tagDao().deleteAssociations(tagId)
            database.tagDao().deleteById(tagId)
        }
    }

    suspend fun associate(expenseId: String, tagId: String) {
        database.expenseTagDao().insert(ExpenseTagCrossRef(expenseId, tagId))
    }

    suspend fun disassociate(expenseId: String, tagId: String) {
        database.expenseTagDao().delete(expenseId, tagId)
    }
}
