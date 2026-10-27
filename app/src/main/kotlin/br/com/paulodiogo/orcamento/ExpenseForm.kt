package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.domain.InvalidMoneyInput
import br.com.paulodiogo.orcamento.domain.DomainError
import br.com.paulodiogo.orcamento.domain.moneyDigitsFromCents
import br.com.paulodiogo.orcamento.domain.parseMoneyDigitsToCents
import br.com.paulodiogo.orcamento.domain.validateExpenseAmount

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateExpenseDialog(
    tags: List<br.com.paulodiogo.orcamento.data.TagEntity>,
    initialDescription: String = "",
    initialAmountCents: Long? = null,
    initialOccurredOn: String = "",
    initialTagIds: Set<String> = emptySet(),
    saving: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, List<String>) -> Unit,
) {
    val emptyInputMessage = stringResource(R.string.money_field_no_digits)
    val invalidInputMessage = stringResource(R.string.money_field_invalid)
    val tooLargeMessage = stringResource(R.string.money_field_too_large)
    val expenseAmountPositiveMessage = stringResource(R.string.expense_amount_positive)
    val requiredFieldsMessage = stringResource(R.string.expense_required_fields)
    val savingMessage = stringResource(R.string.save_in_progress)
    val title = stringResource(if (initialAmountCents == null) R.string.expense_title_new else R.string.expense_title_edit)
    val amountLabel = stringResource(R.string.money_field_label)
    var description by rememberSaveable(initialDescription) { mutableStateOf(initialDescription) }
    var amountDigits by rememberSaveable(initialAmountCents) {
        mutableStateOf(initialAmountCents?.let(::moneyDigitsFromCents) ?: "")
    }
    var occurredOn by rememberSaveable(initialOccurredOn) {
        mutableStateOf(initialOccurredOn.ifBlank { todayIsoDate() })
    }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTagIds by rememberSaveable(initialTagIds) { mutableStateOf(initialTagIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrição") })
                MoneyTextField(
                    digits = amountDigits,
                    onDigitsChange = { amountDigits = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = amountLabel,
                    error = amountError,
                    onInputError = { amountError = it },
                )
                DateField(
                    value = occurredOn,
                    onValueChange = { occurredOn = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Data",
                    testTag = "expenseDateField",
                )
                FlowRow {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = tag.id in selectedTagIds,
                            onClick = {
                                selectedTagIds = if (tag.id in selectedTagIds) {
                                    selectedTagIds - tag.id
                                } else {
                                    selectedTagIds + tag.id
                                }
                            },
                            label = { Text(tag.name) },
                        )
                    }
                }
                error?.let { Text(it) }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    if (description.isBlank() || amountDigits.isEmpty() || occurredOn.isBlank()) {
                        if (amountDigits.isEmpty()) amountError = emptyInputMessage
                        error = requiredFieldsMessage
                    } else {
                        try {
                            val amountCents = parseMoneyDigitsToCents(amountDigits)
                            validateExpenseAmount(amountCents)
                            amountError = null
                            error = null
                            onSave(description, amountCents, occurredOn, selectedTagIds.toList())
                        } catch (exception: InvalidMoneyInput) {
                            amountError = exception.toDisplayMessage(emptyInputMessage, invalidInputMessage, tooLargeMessage)
                        } catch (exception: DomainError.InvalidAmount) {
                            amountError = expenseAmountPositiveMessage
                        } catch (exception: IllegalArgumentException) {
                            amountError = invalidInputMessage
                        }
                    }
                },
            ) { Text(if (saving) savingMessage else "Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") } },
    )
}
