package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.domain.TagState
import br.com.paulodiogo.orcamento.domain.TagViewModel
import br.com.paulodiogo.orcamento.domain.moneyDigitsFromCents
import br.com.paulodiogo.orcamento.domain.parseMoneyDigitsToCents

@Composable
fun TagManagementScreen(viewModel: TagViewModel, padding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val invalidBudgetMessage = stringResource(R.string.tag_budget_invalid)
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingName by rememberSaveable { mutableStateOf("") }
    var editingBudgetDigits by rememberSaveable { mutableStateOf("") }
    var budgetError by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    ScreenContent(padding, horizontalPadding = 20.dp) {
        ScreenTitle("Tags", "Organize seus gastos por categoria")
        Button(onClick = {
            editingId = null
            editingName = ""
            editingBudgetDigits = ""
            budgetError = null
            showEditor = true
        }, modifier = Modifier.fillMaxWidth().testTag("createTagButton")) { Text("Criar nova tag") }
        when (val current = state) {
            TagState.Loading -> CircularProgressIndicator()
            TagState.Empty -> EmptyState("Nenhuma tag criada", "Crie categorias como alimentação, transporte ou casa.")
            is TagState.Error -> Text(current.message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            is TagState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(current.tags, key = { it.id }) { tag ->
                    SummaryCard(tag.name, "Categoria de gastos", "Nome normalizado: ${tag.normalizedName}", Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            editingId = tag.id
                            editingName = tag.name
                            editingBudgetDigits = tag.budgetAmountCents?.let(::moneyDigitsFromCents) ?: ""
                            budgetError = null
                            showEditor = true
                        }) { Text("Editar") }
                        TextButton(onClick = { deleteId = tag.id }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingId == null) "Nova tag" else "Editar tag") },
            text = {
                Column(Modifier.testTag("tagEditor"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(editingName, { editingName = it }, modifier = Modifier.testTag("tagNameField"), label = { Text("Nome") }, singleLine = true)
                    MoneyTextField(
                        digits = editingBudgetDigits,
                        onDigitsChange = { editingBudgetDigits = it },
                        modifier = Modifier.testTag("tagBudgetField"),
                        label = stringResource(R.string.tag_budget_label),
                        error = budgetError,
                        onInputError = { budgetError = it },
                    )
                    Text(stringResource(R.string.tag_budget_supporting), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    budgetError = null
                    val budget: Long? = try {
                        if (editingBudgetDigits.isEmpty()) null else parseMoneyDigitsToCents(editingBudgetDigits)
                    } catch (_: Exception) {
                        budgetError = invalidBudgetMessage
                        null
                    }
                    if (budgetError == null) {
                        viewModel.save(editingName, budget, editingId, onComplete = { showEditor = false }, onError = { budgetError = it.message })
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton({ showEditor = false }) { Text("Cancelar") } },
        )
    }
    deleteId?.let { id ->
        AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Excluir tag?") }, text = { Text("As associações desta tag serão removidas dos lançamentos.") }, confirmButton = { Button(onClick = { viewModel.delete(id) { deleteId = null } }, modifier = Modifier.testTag("confirmDeleteTagButton")) { Text("Excluir") } }, dismissButton = { TextButton({ deleteId = null }) { Text("Cancelar") } })
    }
}
