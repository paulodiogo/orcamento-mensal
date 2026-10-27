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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateExpenseDialog(
    tags: List<br.com.paulodiogo.orcamento.data.TagEntity>,
    initialDescription: String = "",
    initialAmount: String = "",
    initialOccurredOn: String = "",
    initialTagIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onSave: (String, String, String, List<String>) -> Unit,
) {
    var description by remember { mutableStateOf(initialDescription) }
    var amount by remember { mutableStateOf(initialAmount) }
    var occurredOn by remember { mutableStateOf(initialOccurredOn) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTagIds by remember { mutableStateOf(initialTagIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo lançamento") },
        text = {
            Column {
                OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrição") })
                OutlinedTextField(amount, { amount = it }, Modifier.fillMaxWidth(), label = { Text("Valor") })
                OutlinedTextField(occurredOn, { occurredOn = it }, Modifier.fillMaxWidth(), label = { Text("Data (AAAA-MM-DD)") })
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
            Button(onClick = {
                if (description.isBlank() || amount.isBlank() || occurredOn.isBlank()) error = "Preencha todos os campos"
                else onSave(description, amount, occurredOn, selectedTagIds.toList())
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
