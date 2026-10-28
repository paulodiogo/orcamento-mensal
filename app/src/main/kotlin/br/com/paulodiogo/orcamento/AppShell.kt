package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import br.com.paulodiogo.orcamento.R
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import br.com.paulodiogo.orcamento.data.LocalBackupManager
import br.com.paulodiogo.orcamento.data.TagEntity
import br.com.paulodiogo.orcamento.domain.BudgetState
import br.com.paulodiogo.orcamento.domain.BudgetViewModel
import br.com.paulodiogo.orcamento.domain.ExpenseListItem
import br.com.paulodiogo.orcamento.domain.ExpenseListState
import br.com.paulodiogo.orcamento.domain.ExpenseListViewModel
import br.com.paulodiogo.orcamento.domain.ReportState
import br.com.paulodiogo.orcamento.domain.ReportViewModel
import br.com.paulodiogo.orcamento.domain.TagState
import br.com.paulodiogo.orcamento.domain.TagViewModel
import java.text.NumberFormat
import java.util.Locale
import java.time.YearMonth

private enum class MainDestination { HOME, EXPENSES, REPORTS, BUDGETS, TAGS, BACKUP }

@Composable
fun AppShell(
    expenses: ExpenseListViewModel,
    reports: ReportViewModel,
    budgets: BudgetViewModel,
    tagViewModel: TagViewModel,
) {
    var destination by rememberSaveable { mutableStateOf(MainDestination.HOME) }
    var showCreate by rememberSaveable { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backupManager = androidx.compose.runtime.remember(context) { LocalBackupManager(context) }
    var backupMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var lastBackupName by rememberSaveable { mutableStateOf<String?>(null) }
    var backupBusy by rememberSaveable { mutableStateOf(false) }
    val tags by expenses.tags.collectAsState()
    val editing by expenses.editing.collectAsState()
    val expenseSaving by expenses.saving.collectAsState()
    val todayMonth = rememberSaveable { YearMonth.now().toString() }
    var showRestoreConfirmation by rememberSaveable { mutableStateOf(false) }
    var selectedTree by rememberSaveable { mutableStateOf<String?>(null) }
    var restoreFolderSelection by rememberSaveable { mutableStateOf(false) }
    var discoveredBackups by androidx.compose.runtime.remember { mutableStateOf<List<br.com.paulodiogo.orcamento.data.AvailableBackup>>(emptyList()) }
    var selectedGeneration by rememberSaveable { mutableStateOf<String?>(null) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        backupManager.lastBackupSummary().onSuccess { lastBackupName = it }
        backupManager.registeredBackupFolder().onSuccess { folder -> selectedTree = folder?.toString() }
    }
    val treePicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val grantedFlags = result.data?.flags ?: 0
                val accessFlags = (if (grantedFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
                    (if (grantedFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0) Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
                if (accessFlags == 0) {
                    backupMessage = "O provedor não concedeu acesso à pasta. Escolha outra pasta."
                } else {
                    runCatching { context.contentResolver.takePersistableUriPermission(uri, accessFlags) }
                        .onSuccess {
                            selectedTree = uri.toString()
                            coroutineScope.launch {
                                val rememberResult = backupManager.rememberBackupFolder(uri)
                                backupMessage = rememberResult.exceptionOrNull()?.message
                                if (restoreFolderSelection && rememberResult.isSuccess) {
                                    restoreFolderSelection = false
                                    backupBusy = true
                                    val result = backupManager.discoverBackups(uri)
                                    discoveredBackups = result.getOrDefault(emptyList())
                                    backupMessage = result.exceptionOrNull()?.message
                                        ?: if (discoveredBackups.isEmpty()) "Nenhum backup válido encontrado nesta pasta." else null
                                    backupBusy = false
                                }
                            }
                        }
                        .onFailure { backupMessage = "Não foi possível manter o acesso à pasta. Escolha outra pasta." }
                }
            }
        }
    }
    fun openBackupFolderPicker() {
        val initialTree = selectedTree?.let(Uri::parse)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            if (initialTree != null && DocumentsContract.isTreeUri(initialTree)) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialTree)
            }
        }
        treePicker.launch(intent)
    }

    BackHandler(enabled = destination == MainDestination.BACKUP) {
        destination = MainDestination.HOME
    }

    BudgetTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { if (!backupBusy) showCreate = true }) {
                    Icon(painterResource(R.drawable.ic_nav_add), "Adicionar lançamento")
                }
            },
            bottomBar = {
                if (destination != MainDestination.BACKUP) NavigationBar {
                    NavigationBarItem(destination == MainDestination.HOME, { destination = MainDestination.HOME }, icon = { Icon(painterResource(R.drawable.ic_nav_home), "Início") }, label = { Text("Início") })
                    NavigationBarItem(destination == MainDestination.EXPENSES, { destination = MainDestination.EXPENSES }, icon = { Icon(painterResource(R.drawable.ic_nav_expenses), "Despesas") }, label = { Text("Despesas") })
                    NavigationBarItem(destination == MainDestination.REPORTS, { destination = MainDestination.REPORTS }, icon = { Icon(painterResource(R.drawable.ic_nav_reports), "Relatórios") }, label = { Text("Relatórios") })
                    NavigationBarItem(destination == MainDestination.BUDGETS, { destination = MainDestination.BUDGETS }, icon = { Icon(painterResource(R.drawable.ic_nav_budget), "Orçamento") }, label = { Text("Orçamento") })
                    NavigationBarItem(destination == MainDestination.TAGS, { destination = MainDestination.TAGS }, icon = { Icon(painterResource(R.drawable.ic_nav_tags), "Tags") }, label = { Text("Tags") })
                }
            },
        ) { padding ->
            when (destination) {
                MainDestination.HOME -> HomeScreen(
                    reports,
                    expenses,
                    todayMonth,
                    padding,
                    onSeeExpenses = { destination = MainDestination.EXPENSES },
                    onBackup = { destination = MainDestination.BACKUP },
                )
                MainDestination.EXPENSES -> ExpenseHistoryScreen(expenses, tags, padding)
                MainDestination.REPORTS -> ReportScreen(reports, padding)
                MainDestination.BUDGETS -> BudgetScreen(budgets, padding)
                MainDestination.TAGS -> TagManagementScreen(tagViewModel, padding)
                MainDestination.BACKUP -> BackupScreen(
                    padding = padding,
                    busy = backupBusy,
                    message = backupMessage,
                    lastBackupName = lastBackupName,
                    folderSelected = selectedTree != null,
                    backups = discoveredBackups,
                    selectedGeneration = selectedGeneration,
                    onSelectBackup = { selectedGeneration = it },
                    onChooseBackupFolder = {
                        restoreFolderSelection = true
                        openBackupFolderPicker()
                    },
                    onBack = { destination = MainDestination.HOME },
                    onCreate = {
                        val uri = selectedTree?.let(Uri::parse)
                        if (uri == null) {
                            openBackupFolderPicker()
                        } else {
                            backupBusy = true
                            coroutineScope.launch {
                                backupMessage = runCatching {
                                        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                        backupManager.rememberBackupFolder(uri).getOrThrow()
                                        backupManager.createBackup(
                                        uri,
                                        pauseObservers = {
                                            expenses.pauseDatabaseObservers()
                                            reports.pauseDatabaseObservers()
                                            tagViewModel.pauseDatabaseObservers()
                                        },
                                        onDatabaseReady = {
                                            val database = br.com.paulodiogo.orcamento.data.BudgetDatabase.getInstance(context)
                                            expenses.onDatabaseReplaced(database)
                                            reports.onDatabaseReplaced(database)
                                            budgets.onDatabaseReplaced(database)
                                            tagViewModel.onDatabaseReplaced(database)
                                        },
                                    )
                                    "Backup criado e verificado."
                                }.getOrElse { "Não foi possível concluir o backup." }
                                backupManager.lastBackupSummary().onSuccess { lastBackupName = it }
                                backupBusy = false
                            }
                        }
                    },
                    onRestore = {
                        if (selectedGeneration != null && discoveredBackups.any { it.generationId == selectedGeneration }) {
                            showRestoreConfirmation = true
                        } else coroutineScope.launch {
                            backupBusy = true
                            val registered = backupManager.registeredBackupFolder().getOrNull()
                            val folder = registered ?: selectedTree?.let(Uri::parse)
                            if (folder == null) {
                                backupBusy = false
                                restoreFolderSelection = true
                                openBackupFolderPicker()
                            } else {
                                val result = backupManager.discoverBackups(folder)
                                if (result.isFailure) {
                                    backupBusy = false
                                    restoreFolderSelection = true
                                    backupMessage = result.exceptionOrNull()?.message
                                    treePicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                                } else {
                                    discoveredBackups = result.getOrDefault(emptyList())
                                    selectedGeneration = null
                                    backupMessage = if (discoveredBackups.isEmpty()) "Nenhum backup válido encontrado nesta pasta." else null
                                    backupBusy = false
                                }
                            }
                        }
                    },
                )
            }
        }
    }

    if (showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmation = false },
            title = { Text("Substituir os dados atuais?") },
            text = { Text("A restauração substituirá os dados financeiros atuais pelos contidos no backup.") },
            confirmButton = {
                TextButton(onClick = {
                    val backup = discoveredBackups.firstOrNull { it.generationId == selectedGeneration }
                    if (backup == null) {
                        backupMessage = "Selecione um backup válido para restaurar."
                        showRestoreConfirmation = false
                        return@TextButton
                    }
                    showRestoreConfirmation = false
                    backupBusy = true
                    coroutineScope.launch {
                        backupMessage = runCatching { backupManager.restoreSelectedBackup(
                            backup,
                            pauseObservers = {
                                expenses.pauseDatabaseObservers()
                                reports.pauseDatabaseObservers()
                                tagViewModel.pauseDatabaseObservers()
                            },
                        ) {
                                val database = br.com.paulodiogo.orcamento.data.BudgetDatabase.getInstance(context)
                                expenses.onDatabaseReplaced(database)
                                reports.onDatabaseReplaced(database)
                                budgets.onDatabaseReplaced(database)
                                tagViewModel.onDatabaseReplaced(database)
                            } }
                                                    .fold(
                                                        { "Restauração concluída." },
                                                        { "Não foi possível restaurar o backup; verifique o estado recuperável." },
                                                    )
                        backupBusy = false
                    }
                }) { Text("Restaurar") }
            },
            dismissButton = { TextButton(onClick = { showRestoreConfirmation = false }) { Text("Cancelar") } },
        )
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
    onBackup: () -> Unit,
) {
    val state by reports.state.collectAsState()
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(month) { reports.load(month) }
    ScreenContent(padding, horizontalPadding = 20.dp, scrollable = true) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                ScreenTitle("Olá, vamos organizar?", "Visão do seu orçamento em $month")
            }
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.semantics { contentDescription = "Mais opções" }) {
                    Text("⋮", style = MaterialTheme.typography.headlineMedium)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Backup e restauração") }, onClick = { menuExpanded = false; onBackup() })
                }
            }
        }
        when (val current = state) {
            ReportState.Idle, ReportState.Loading -> CircularProgressIndicator(Modifier.size(28.dp))
            ReportState.Empty -> EmptyState("Seu mês começa aqui", "Abra um orçamento e adicione seu primeiro lançamento.")
            is ReportState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ReportState.Content -> {
                val budget = current.budgetCents ?: 0L
                SummaryCard("Saldo disponível", formatBrazilianCents(current.execution.availableCents), "de ${formatBrazilianCents(budget)} planejados", Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("Despesas", formatBrazilianCents(current.execution.spentCents), modifier = Modifier.weight(1f))
                    SummaryCard("Consumo", current.execution.percentage?.let { "$it%" } ?: "—", "", Modifier.weight(1f))
                }
                SectionHeader("Orçamento por tag")
                val budgetedTags = current.tagBudgetExecutions.filter { it.budgetCents != null }
                if (budgetedTags.isEmpty()) {
                    Text(stringResource(R.string.tag_budget_home_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    budgetedTags.take(4).forEach { TagBudgetRow(it) }
                }
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
    ScreenContent(padding, horizontalPadding = 20.dp) {
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
                items(current.items, key = { it.expense.id }) { item ->
                    ExpenseHistoryCard(
                        item = item,
                        onEdit = { viewModel.edit(item.expense.id) },
                        onDelete = { deleteId = item.expense.id },
                    )
                }
            }
        }
    }
    deleteId?.let { id -> AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Excluir lançamento?") }, text = { Text("Essa ação não pode ser desfeita.") }, confirmButton = { Button(onClick = { viewModel.delete(id); deleteId = null }) { Text("Excluir") } }, dismissButton = { TextButton({ deleteId = null }) { Text("Cancelar") } }) }
}

@Composable
private fun ExpenseHistoryCard(
    item: ExpenseListItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val expense = item.expense
    val actionDescription = "${expense.description}, ${formatExpenseHistoryDate(expense.occurredOn)}, ${formatBrazilianCents(expense.amountCents)}"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.width(42.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val dateParts = formatExpenseHistoryDate(expense.occurredOn).split(" ", limit = 2)
                Text(dateParts.firstOrNull().orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(dateParts.getOrNull(1).orEmpty(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    maxItemsInEachRow = Int.MAX_VALUE,
                ) {
                    Text(
                        expense.description,
                        modifier = Modifier.heightIn(min = 24.dp),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.tags.forEach { tag ->
                        Surface(
                            modifier = Modifier.heightIn(min = 24.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                tag.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                Text(
                    formatBrazilianCents(expense.amountCents),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.semantics { contentDescription = "Editar lançamento $actionDescription" },
                ) {
                    Icon(painterResource(R.drawable.ic_edit), contentDescription = null)
                }
                Text("Editar", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "Excluir lançamento $actionDescription" },
                ) {
                    Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
                Text("Excluir", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
