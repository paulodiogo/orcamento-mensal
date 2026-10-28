package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.DatabaseOperationGate
import br.com.paulodiogo.orcamento.data.BudgetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BudgetState {
    data object Loading : BudgetState
    data class Content(val budgets: List<BudgetEntity>) : BudgetState
    data object Empty : BudgetState
    data class Error(val message: String) : BudgetState
}

data class MonthOpeningState(
    val month: String,
    val amountDigits: String = "",
    val suggested: Long? = null,
    val error: String? = null,
    val saved: Boolean = false,
)

class BudgetViewModel(private var database: BudgetDatabase) : ViewModel() {
    private var operations = BudgetOperations(database)
    private val _state = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val state: StateFlow<BudgetState> = _state.asStateFlow()
    private val _opening = MutableStateFlow<MonthOpeningState?>(null)
    val opening: StateFlow<MonthOpeningState?> = _opening.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            runCatching { DatabaseOperationGate.read { operations.findAll() } }
                .onSuccess { budgets -> _state.value = if (budgets.isEmpty()) BudgetState.Empty else BudgetState.Content(budgets) }
                .onFailure { _state.value = BudgetState.Error(it.message ?: "Não foi possível carregar os meses") }
        }
    }

    fun onDatabaseReplaced(database: BudgetDatabase) {
        this.database = database
        operations = BudgetOperations(database)
        _opening.value = null
        refresh()
    }

    fun openMonth(month: String) {
        viewModelScope.launch {
            runCatching {
                DatabaseOperationGate.read {
                    val existing = operations.findByMonth(month)
                    val suggestion = existing?.amountCents ?: operations.suggestedAmountFor(month)
                    MonthOpeningState(
                        validateMonth(month),
                        existing?.amountCents?.let(::moneyDigitsFromCents) ?: suggestion?.let(::moneyDigitsFromCents) ?: "",
                        suggestion,
                    )
                }
            }.onSuccess { _opening.value = it }
                .onFailure { _opening.value = MonthOpeningState(month, error = it.message ?: "Mês inválido") }
        }
    }

    fun saveMonth(month: String, amountCents: Long, onComplete: () -> Unit = {}) {
        if (_saving.value) return
        _saving.value = true
        viewModelScope.launch {
            try {
                runCatching { operations.save(month, amountCents) }
                    .onSuccess { _opening.value = null; refresh(); onComplete() }
                    .onFailure { _opening.value = (_opening.value ?: MonthOpeningState(month)).copy(error = it.message ?: "Orçamento inválido") }
            } finally {
                _saving.value = false
            }
        }
    }

    fun closeOpening() { _opening.value = null }

    class Factory(private val database: BudgetDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BudgetViewModel(database) as T
    }
}
