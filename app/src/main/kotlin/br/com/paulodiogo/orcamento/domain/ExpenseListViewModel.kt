package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.ExpenseEntity
import br.com.paulodiogo.orcamento.data.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExpenseListState {
    data object Loading : ExpenseListState
    data class Content(val expenses: List<ExpenseEntity>) : ExpenseListState
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

class ExpenseListViewModel(private val database: BudgetDatabase) : ViewModel() {
    private val operations = ExpenseOperations(database)
    private val _state = MutableStateFlow<ExpenseListState>(ExpenseListState.Loading)
    val state: StateFlow<ExpenseListState> = _state.asStateFlow()
    private val _tags = MutableStateFlow<List<TagEntity>>(emptyList())
    val tags: StateFlow<List<TagEntity>> = _tags.asStateFlow()
    private val _editing = MutableStateFlow<Pair<ExpenseEntity, Set<String>>?>(null)
    val editing: StateFlow<Pair<ExpenseEntity, Set<String>>?> = _editing.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private var currentQuery = ""
    private var currentMonth: String? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null
    private var currentTagId: String? = null
    private var currentWithoutTag = false

    init {
        refresh()
        refreshTags()
    }

    fun refreshTags() {
        viewModelScope.launch {
            _tags.value = database.tagDao().findAll()
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
        viewModelScope.launch {
            _state.value = ExpenseListState.Loading
            runCatching {
                val expenses = operations.findAll()
                val relations = expenses.associate { it.id to database.expenseTagDao().findTagIdsForExpense(it.id).toSet() }
                filterExpenses(expenses, query, month, startDate, endDate, relations, tagId, withoutTag)
            }.onSuccess { expenses ->
                _state.value = if (expenses.isEmpty()) ExpenseListState.Empty else ExpenseListState.Content(expenses)
            }.onFailure { error ->
                _state.value = ExpenseListState.Error(error.message ?: "Não foi possível carregar os lançamentos")
            }
        }
    }

    fun clearFilters() = refresh("", null, null, null, null, false)

    fun edit(id: String) {
        viewModelScope.launch {
            _editing.value = operations.findByIdWithTags(id)?.let { it.first to it.second.toSet() }
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
                    .onSuccess { onComplete(); refresh(); refreshTags() }
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
                    operations.update(expense, description, amountCents, occurredOn)
                    operations.updateTags(expense.id, tagIds)
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
