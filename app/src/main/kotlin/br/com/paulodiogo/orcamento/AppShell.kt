package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.data.TagEntity
import br.com.paulodiogo.orcamento.domain.BudgetState
import br.com.paulodiogo.orcamento.domain.BudgetViewModel
import br.com.paulodiogo.orcamento.domain.ExpenseListState
import br.com.paulodiogo.orcamento.domain.ExpenseListViewModel
import br.com.paulodiogo.orcamento.domain.ReportState
import br.com.paulodiogo.orcamento.domain.ReportViewModel
import br.com.paulodiogo.orcamento.domain.TagState
import br.com.paulodiogo.orcamento.domain.TagViewModel
import java.time.YearMonth

private enum class MainDestination { HOME, EXPENSES, REPORTS, BUDGETS, TAGS }

@Composable
fun AppShell(
    expenses: ExpenseListViewModel,
    reports: ReportViewModel,
    budgets: BudgetViewModel,
    tagViewModel: TagViewModel,
) {
    var destination by rememberSaveable { mutableStateOf(MainDestination.HOME) }
    var showCreate by rememberSaveable { mutableStateOf(false) }
    val tags by expenses.tags.collectAsState()
    val editing by expenses.editing.collectAsState()
    val expenseSaving by expenses.saving.collectAsState()
    val todayMonth = rememberSaveable { YearMonth.now().toString() }

    BudgetTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showCreate = true }) {
                    Icon(painterResource(R.drawable.ic_nav_add), "Adicionar lançamento")
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(destination == MainDestination.HOME, { destination = MainDestination.HOME }, icon = { Icon(painterResource(R.drawable.ic_nav_home), "Início") }, label = { Text("Início") })
                    NavigationBarItem(destination == MainDestination.EXPENSES, { destination = MainDestination.EXPENSES }, icon = { Icon(painterResource(R.drawable.ic_nav_expenses), "Despesas") }, label = { Text("Despesas") })
                    NavigationBarItem(destination == MainDestination.REPORTS, { destination = MainDestination.REPORTS }, icon = { Icon(painterResource(R.drawable.ic_nav_reports), "Relatórios") }, label = { Text("Relatórios") })
                    NavigationBarItem(destination == MainDestination.BUDGETS, { destination = MainDestination.BUDGETS }, icon = { Icon(painterResource(R.drawable.ic_nav_budget), "Orçamento") }, label = { Text("Orçamento") })
                    NavigationBarItem(destination == MainDestination.TAGS, { destination = MainDestination.TAGS }, icon = { Icon(painterResource(R.drawable.ic_nav_tags), "Tags") }, label = { Text("Tags") })
                }
            },
        ) { padding ->
            when (destination) {
                MainDestination.HOME -> HomeScreen(reports, expenses, todayMonth, padding) { destination = MainDestination.EXPENSES }
                MainDestination.EXPENSES -> ExpenseHistoryScreen(expenses, tags, padding)
                MainDestination.REPORTS -> ReportScreen(reports, {})
                MainDestination.BUDGETS -> BudgetScreen(budgets, {})
                MainDestination.TAGS -> TagManagementScreen(tagViewModel, padding)
            }
        }
    }

    if (showCreate) {
        CreateExpenseDialog(tags = tags, saving = expenseSaving, onDismiss = { showCreate = false }, onManageTags = { showCreate = false; destination = MainDestination.TAGS }, onSave = { description, amountCents, date, selectedTags ->
            expenses.create(description, amountCents, date, selectedTags) { showCreate = false }
        })
    }
    editing?.let { (expense, selectedTags) ->
        CreateExpenseDialog(
            tags = tags,
            initialDescription = expense.description,
            initialAmountCents = expense.amountCents,
            initialOccurredOn = expense.occurredOn,
            initialTagIds = selectedTags,
            saving = expenseSaving,
            onDismiss = { expenses.clearEditing() },
            onManageTags = { expenses.clearEditing(); destination = MainDestination.TAGS },
        ) { description, amountCents, date, tagIds ->
            expenses.updateExpense(expense, description, amountCents, date, tagIds) { expenses.clearEditing() }
        }
    }
}

@Composable
private fun HomeScreen(
    reports: ReportViewModel,
    expenses: ExpenseListViewModel,
    month: String,
    padding: PaddingValues,
    onSeeExpenses: () -> Unit,
) {
    val state by reports.state.collectAsState()
    LaunchedEffect(month) { reports.load(month) }
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenTitle("Olá, vamos organizar?", "Visão do seu orçamento em $month")
        when (val current = state) {
            ReportState.Idle, ReportState.Loading -> CircularProgressIndicator(Modifier.size(28.dp))
            ReportState.Empty -> EmptyState("Seu mês começa aqui", "Abra um orçamento e adicione seu primeiro lançamento.")
            is ReportState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ReportState.Content -> {
                val budget = current.budgetCents ?: 0L
                SummaryCard("Saldo disponível", formatBrazilianCents(current.execution.availableCents), "de ${formatBrazilianCents(budget)} planejados", Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Despesas", formatBrazilianCents(current.execution.spentCents), modifier = Modifier.weight(1f))
                    SummaryCard("Consumo", current.execution.percentage?.let { "$it%" } ?: "—", "do orçamento", Modifier.weight(1f))
                }
                SectionHeader("Onde você gastou")
                if (current.tagTotals.isEmpty()) Text("Nenhuma despesa por tag neste mês", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else current.tagTotals.take(4).forEach { total ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(total.tagName)
                        Text(formatBrazilianCents(total.amountCents), fontWeight = FontWeight.SemiBold)
                    }
                }
                SectionHeader("Ações rápidas")
                Button(onClick = onSeeExpenses, modifier = Modifier.fillMaxWidth()) { Text("Ver todos os lançamentos") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseHistoryScreen(viewModel: ExpenseListViewModel, tags: List<TagEntity>, padding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTag by rememberSaveable { mutableStateOf<String?>(null) }
    var withoutTag by rememberSaveable { mutableStateOf(false) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenTitle("Lançamentos", "Pesquise e encontre qualquer movimento")
        OutlinedTextField(query, { query = it; viewModel.refresh(query = it, tagId = selectedTag, withoutTag = withoutTag) }, Modifier.fillMaxWidth(), label = { Text("Pesquisar por descrição") }, singleLine = true)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(withoutTag, { withoutTag = !withoutTag; viewModel.refresh(query = query, tagId = selectedTag, withoutTag = withoutTag) }, label = { Text("Sem tag") })
            tags.forEach { tag -> FilterChip(selectedTag == tag.id, { selectedTag = if (selectedTag == tag.id) null else tag.id; viewModel.refresh(query = query, tagId = selectedTag, withoutTag = withoutTag) }, label = { Text(tag.name) }) }
        }
        when (val current = state) {
            ExpenseListState.Loading -> CircularProgressIndicator()
            ExpenseListState.Empty -> EmptyState(if (query.isBlank()) "Nenhum lançamento ainda" else "Nenhum resultado", "Use o botão + para registrar uma despesa ou receita.")
            is ExpenseListState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ExpenseListState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current.expenses, key = { it.id }) { expense ->
                    SummaryCard(expense.description, formatBrazilianCents(expense.amountCents), expense.occurredOn, Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.edit(expense.id) }) { Text("Editar") }
                        TextButton(onClick = { deleteId = expense.id }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    deleteId?.let { id -> AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Excluir lançamento?") }, text = { Text("Essa ação não pode ser desfeita.") }, confirmButton = { Button(onClick = { viewModel.delete(id); deleteId = null }) { Text("Excluir") } }, dismissButton = { TextButton({ deleteId = null }) { Text("Cancelar") } }) }
}
