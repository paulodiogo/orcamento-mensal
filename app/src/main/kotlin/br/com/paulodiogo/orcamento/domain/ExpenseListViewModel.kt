package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.DatabaseOperationGate
import br.com.paulodiogo.orcamento.data.ExpenseEntity
import br.com.paulodiogo.orcamento.data.ExpenseTagListRow
import br.com.paulodiogo.orcamento.data.TagEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ExpenseListTag(
    val id: String,
    val name: String,
)

data class ExpenseListItem(
    val expense: ExpenseEntity,
    val tags: List<ExpenseListTag>,
)

sealed interface ExpenseListState {
    data object Loading : ExpenseListState
    data class Content(val expenses: List<ExpenseEntity>, val items: List<ExpenseListItem> = emptyList()) : ExpenseListState
    data object Empty : ExpenseListState
    data class Error(val message: String) : ExpenseListState
}

data class ExpenseFormState(
    val description: String = "",
    val amount: String = "",
    val occurredOn: String = "",
    val error: String? = null,
    val saved: Boolean = false,
)

class ExpenseListViewModel(private var database: BudgetDatabase) : ViewModel() {
    private var operations = ExpenseOperations(database)
    private val _state = MutableStateFlow<ExpenseListState>(ExpenseListState.Loading)
    val state: StateFlow<ExpenseListState> = _state.asStateFlow()
    private val _tags = MutableStateFlow<List<TagEntity>>(emptyList())
    val tags: StateFlow<List<TagEntity>> = _tags.asStateFlow()
    private val _editing = MutableStateFlow<Pair<ExpenseEntity, Set<String>>?>(null)
    val editing: StateFlow<Pair<ExpenseEntity, Set<String>>?> = _editing.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()
    private var currentExpenses: List<ExpenseEntity> = emptyList()

    private var currentQuery = ""
    private var currentMonth: String? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null
    private var currentTagId: String? = null
    private var currentWithoutTag = false

    private var observationJob: Job? = null

    init {
        observeDatabase()
    }

    private fun observeDatabase() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            runCatching {
                kotlinx.coroutines.flow.combine(database.expenseDao().observeAllWithTags(), database.tagDao().observeAll()) { rows, tags ->
                    rows to tags
                }.collect { (rows, tags) ->
                    _tags.value = tags
                    currentTagId?.let { selected -> if (tags.none { it.id == selected }) currentTagId = null }
                    DatabaseOperationGate.read { publish(rows) }
                }
            }.onFailure { _state.value = ExpenseListState.Error(it.message ?: "Não foi possível carregar os dados") }
        }
    }

    suspend fun pauseDatabaseObservers() {
        observationJob?.cancelAndJoin()
    }

    fun onDatabaseReplaced(database: BudgetDatabase) {
        this.database = database
        operations = ExpenseOperations(database)
        _editing.value = null
        _state.value = ExpenseListState.Loading
        observeDatabase()
    }

    private suspend fun publish(rows: List<ExpenseTagListRow>) {
        val grouped = rows.groupBy { it.id }
        val expenses = grouped.values.map { entries ->
            val first = entries.first()
            ExpenseEntity(first.id, first.description, first.amountCents, first.occurredOn, first.createdAt, first.updatedAt)
        }
        currentExpenses = expenses
        runCatching {
            val relations = grouped.mapValues { (_, entries) -> entries.mapNotNull { it.tagId }.toSet() }
            val tagById = rows.mapNotNull { row ->
                val id = row.tagId
                val name = row.tagName
                if (id == null || name == null) null else id to ExpenseListTag(id, name)
            }.toMap()
            val filtered = filterExpenses(
                expenses,
                currentQuery,
                currentMonth,
                currentStartDate,
                currentEndDate,
                relations,
                currentTagId,
                currentWithoutTag,
            )
            filtered.map { expense ->
                ExpenseListItem(expense, relations[expense.id].orEmpty().mapNotNull(tagById::get))
            }
        }.onSuccess { filtered ->
            _state.value = if (filtered.isEmpty()) ExpenseListState.Empty else ExpenseListState.Content(
                expenses = filtered.map { it.expense },
                items = filtered,
            )
        }.onFailure { error ->
            _state.value = ExpenseListState.Error(error.message ?: "Não foi possível carregar os lançamentos")
        }
    }

    fun refresh(
        query: String = currentQuery,
        month: String? = currentMonth,
        startDate: String? = currentStartDate,
        endDate: String? = currentEndDate,
        tagId: String? = currentTagId,
        withoutTag: Boolean = currentWithoutTag,
    ) {
        currentQuery = query
        currentMonth = month
        currentStartDate = startDate
        currentEndDate = endDate
        currentTagId = tagId
        currentWithoutTag = withoutTag
        _state.value = ExpenseListState.Loading
        viewModelScope.launch {
            DatabaseOperationGate.read { publish(database.expenseDao().findAllWithTags()) }
        }
    }

    fun clearFilters() = refresh("", null, null, null, null, false)

    fun onDatabaseReplaced() {
        _editing.value = null
        refresh()
    }

    fun edit(id: String) {
        viewModelScope.launch {
            _editing.value = DatabaseOperationGate.read { operations.findByIdWithTags(id)?.let { it.first to it.second.toSet() } }
        }
    }

    fun clearEditing() {
        _editing.value = null
    }

    fun create(description: String, amountCents: Long, occurredOn: String, tagIds: List<String> = emptyList(), onComplete: () -> Unit = {}) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                runCatching { operations.create(description, amountCents, occurredOn, tagIds = tagIds) }
                    .onSuccess { onComplete(); refresh() }
                    .onFailure { error -> _state.value = ExpenseListState.Error(error.message ?: "Não foi possível salvar o lançamento") }
            } finally {
                _saving.value = false
            }
        }
    }

    fun updateExpense(
        expense: ExpenseEntity,
        description: String,
        amountCents: Long,
        occurredOn: String,
        tagIds: List<String>,
        onComplete: () -> Unit = {},
    ) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                runCatching {
                    DatabaseOperationGate.write {
                        operations.update(expense, description, amountCents, occurredOn)
                        operations.updateTags(expense.id, tagIds)
                    }
                }.onSuccess { onComplete(); refresh() }
                    .onFailure { error -> _state.value = ExpenseListState.Error(error.message ?: "Não foi possível atualizar o lançamento") }
            } finally {
                _saving.value = false
            }
        }
    }

    fun updateTags(expenseId: String, tagIds: List<String>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { operations.updateTags(expenseId, tagIds) }
                .onSuccess { onComplete(); refresh() }
                .onFailure { error -> _state.value = ExpenseListState.Error(error.message ?: "Não foi possível atualizar as tags") }
        }
    }

    fun delete(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { operations.delete(id) }
                            .onSuccess { onComplete(); refresh() }
                .onFailure { error -> _state.value = ExpenseListState.Error(error.message ?: "Não foi possível excluir o lançamento") }
        }
    }

    class Factory(private val database: BudgetDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExpenseListViewModel(database) as T
    }
}
