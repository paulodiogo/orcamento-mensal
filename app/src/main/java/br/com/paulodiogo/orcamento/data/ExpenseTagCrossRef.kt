package br.com.paulodiogo.orcamento.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "expense_tag",
    primaryKeys = ["expenseId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["tagId"])],
)
data class ExpenseTagCrossRef(
    val expenseId: String,
    val tagId: String,
)
