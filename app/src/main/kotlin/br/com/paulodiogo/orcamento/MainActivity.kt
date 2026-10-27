package br.com.paulodiogo.orcamento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.ExpenseListState
import br.com.paulodiogo.orcamento.domain.ExpenseListViewModel
import br.com.paulodiogo.orcamento.domain.ReportViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseListViewModel by viewModels {
        ExpenseListViewModel.Factory(BudgetDatabase.getInstance(applicationContext))
    }
    private val reportViewModel: ReportViewModel by viewModels {
        ReportViewModel.Factory(BudgetDatabase.getInstance(applicationContext))
    }
    private val budgetViewModel: br.com.paulodiogo.orcamento.domain.BudgetViewModel by viewModels {
        br.com.paulodiogo.orcamento.domain.BudgetViewModel.Factory(BudgetDatabase.getInstance(applicationContext))
    }
    private val tagViewModel: br.com.paulodiogo.orcamento.domain.TagViewModel by viewModels {
        br.com.paulodiogo.orcamento.domain.TagViewModel.Factory(BudgetDatabase.getInstance(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = isSystemInDarkTheme()
            Surface {
                AppShell(viewModel, reportViewModel, budgetViewModel, tagViewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseListScreen(viewModel: ExpenseListViewModel, onReports: () -> Unit, onBudgets: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var withoutTag by rememberSaveable { mutableStateOf(false) }
    var selectedTagId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMonth by rememberSaveable { mutableStateOf("") }
    var rangeStart by rememberSaveable { mutableStateOf("") }
    var rangeEnd by rememberSaveable { mutableStateOf("") }
    val tags by viewModel.tags.collectAsState()
    val editing by viewModel.editing.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) { Text("+") }
        },
    ) { paddingValues ->
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Lançamentos", style = MaterialTheme.typography.headlineSmall)
            Row {
                TextButton(onClick = onReports) { Text("Relatórios") }
                TextButton(onClick = onBudgets) { Text("Orçamentos") }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.refresh(query = it, tagId = selectedTagId, withoutTag = withoutTag) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pesquisar por descrição") },
            singleLine = true,
        )
        OutlinedTextField(
            value = selectedMonth,
            onValueChange = { selectedMonth = it; viewModel.refresh(query = query, month = it.ifBlank { null }, startDate = rangeStart.ifBlank { null }, endDate = rangeEnd.ifBlank { null }, tagId = selectedTagId, withoutTag = withoutTag) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mês (AAAA-MM)") },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rangeStart,
                onValueChange = { rangeStart = it; viewModel.refresh(query = query, month = selectedMonth.ifBlank { null }, startDate = it.ifBlank { null }, endDate = rangeEnd.ifBlank { null }, tagId = selectedTagId, withoutTag = withoutTag) },
                modifier = Modifier.weight(1f),
                label = { Text("De") },
                singleLine = true,
            )
            OutlinedTextField(
                value = rangeEnd,
                onValueChange = { rangeEnd = it; viewModel.refresh(query = query, month = selectedMonth.ifBlank { null }, startDate = rangeStart.ifBlank { null }, endDate = it.ifBlank { null }, tagId = selectedTagId, withoutTag = withoutTag) },
                modifier = Modifier.weight(1f),
                label = { Text("Até") },
                singleLine = true,
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = withoutTag,
                onClick = { withoutTag = !withoutTag; viewModel.refresh(query = query, tagId = selectedTagId, withoutTag = withoutTag) },
                label = { Text("Sem tag") },
            )
            tags.forEach { tag ->
                FilterChip(
                    selected = selectedTagId == tag.id,
                    onClick = {
                        selectedTagId = if (selectedTagId == tag.id) null else tag.id
                        viewModel.refresh(query = query, tagId = selectedTagId, withoutTag = withoutTag)
                    },
                    label = { Text(tag.name) },
                )
            }
            if (query.isNotBlank() || selectedMonth.isNotBlank() || rangeStart.isNotBlank() || rangeEnd.isNotBlank() || withoutTag || selectedTagId != null) {
                TextButton(onClick = { query = ""; selectedMonth = ""; rangeStart = ""; rangeEnd = ""; withoutTag = false; selectedTagId = null; viewModel.clearFilters() }) {
                    Text("Limpar filtros")
                }
            }
        }
        when (val current = state) {
            ExpenseListState.Loading -> CircularProgressIndicator()
            ExpenseListState.Empty -> Text(if (query.isBlank()) "Nenhum lançamento" else "Nenhum resultado")
            is ExpenseListState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ExpenseListState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current.expenses, key = { it.id }) { expense ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(expense.description)
                            Text("${expense.occurredOn} • ${expense.amountCents} centavos")
                        }
                        Row {
                            TextButton(onClick = { viewModel.edit(expense.id) }) { Text("Editar") }
                            TextButton(onClick = { showDeleteDialog = expense.id }) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }
    }

    if (showCreateDialog) {
        CreateExpenseDialog(
            tags = tags,
            onDismiss = { showCreateDialog = false },
            onSave = { description, amount, date, tagIds ->
                viewModel.create(description, amount, date, tagIds) { showCreateDialog = false }
            },
        )
    }

    editing?.let { (expense, tagIds) ->
        CreateExpenseDialog(
            tags = tags,
            initialDescription = expense.description,
            initialAmount = java.math.BigDecimal.valueOf(expense.amountCents, 2).toPlainString(),
            initialOccurredOn = expense.occurredOn,
            initialTagIds = tagIds,
            onDismiss = { viewModel.clearEditing() },
            onSave = { description, amount, date, selectedTags ->
                viewModel.updateExpense(expense, description, amount, date, selectedTags) { viewModel.clearEditing() }
            },
        )
    }

    showDeleteDialog?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir lançamento?") },
            text = { Text("Essa ação não pode ser desfeita.") },
            confirmButton = {
                Button(onClick = { viewModel.delete(id); showDeleteDialog = null }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") } },
        )
    }
}
