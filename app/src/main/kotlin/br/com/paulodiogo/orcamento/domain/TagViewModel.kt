package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.DatabaseOperationGate
import br.com.paulodiogo.orcamento.data.TagEntity
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin

sealed interface TagState {
    data object Loading : TagState
    data class Content(val tags: List<TagBudgetExecution>) : TagState
    data object Empty : TagState
    data class Error(val message: String) : TagState
}

class TagViewModel(private var database: BudgetDatabase) : ViewModel() {
    private var operations = TagOperations(database)
    private val _state = MutableStateFlow<TagState>(TagState.Loading)
    val state: StateFlow<TagState> = _state.asStateFlow()

    init { observeDatabase() }

    fun refresh() {
        // The Room flow remains authoritative; this method is kept for existing callers.
    }

    fun onDatabaseReplaced(database: BudgetDatabase) {
        this.database = database
        operations = TagOperations(database)
        _state.value = TagState.Loading
        observeDatabase()
    }

    private var observationJob: kotlinx.coroutines.Job? = null

    suspend fun pauseDatabaseObservers() {
        observationJob?.cancelAndJoin()
    }

    private fun observeDatabase() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            runCatching {
                val startDate = YearMonth.now().atDay(1).toString()
                val endDate = YearMonth.now().atEndOfMonth().toString()
                combine(
                    database.tagDao().observeAll(),
                    database.expenseDao().observeTagBudgetUsageBetween(startDate, endDate),
                ) { tagEntities, usageRows ->
                    val usageByTagId = usageRows.associateBy { it.tagId }
                    tagEntities.map { tag ->
                        val usage = usageByTagId[tag.id]
                        calculateTagBudgetExecution(
                            tagId = tag.id,
                            tagName = tag.name,
                            budgetCents = tag.budgetAmountCents,
                            spentCents = usage?.spentCents ?: 0L,
                        )
                    }
                }.collect { tags ->
                    _state.value = if (tags.isEmpty()) TagState.Empty else TagState.Content(tags)
                }
            }.onFailure { _state.value = TagState.Error(it.message ?: "Não foi possível carregar as tags") }
        }
    }

    fun save(
        name: String,
        budgetAmountCents: Long? = null,
        editingId: String? = null,
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    ) {
        viewModelScope.launch {
            runCatching {
                DatabaseOperationGate.write {
                    if (editingId == null) operations.create(name, budgetAmountCents)
                    else operations.update(editingId, name, budgetAmountCents)
                }
            }.onSuccess { refresh(); onComplete() }
                .onFailure {
                    _state.value = TagState.Error(it.message ?: "Não foi possível salvar a tag")
                    onError(it)
                }
        }
    }

    fun delete(id: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { DatabaseOperationGate.write { operations.delete(id) } }
                .onSuccess { refresh(); onComplete() }
                .onFailure { _state.value = TagState.Error(it.message ?: "Não foi possível excluir a tag") }
        }
    }

    class Factory(private val database: BudgetDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TagViewModel(database) as T
    }
}
