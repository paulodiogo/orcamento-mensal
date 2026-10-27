package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
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
    val amount: String = "",
    val suggested: Long? = null,
    val error: String? = null,
    val saved: Boolean = false,
)

class BudgetViewModel(private val database: BudgetDatabase) : ViewModel() {
    private val operations = BudgetOperations(database)
    private val _state = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val state: StateFlow<BudgetState> = _state.asStateFlow()
    private val _opening = MutableStateFlow<MonthOpeningState?>(null)
    val opening: StateFlow<MonthOpeningState?> = _opening.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            runCatching { operations.findAll() }
                .onSuccess { budgets -> _state.value = if (budgets.isEmpty()) BudgetState.Empty else BudgetState.Content(budgets) }
                .onFailure { _state.value = BudgetState.Error(it.message ?: "Não foi possível carregar os meses") }
        }
    }

    fun openMonth(month: String) {
        viewModelScope.launch {
            runCatching {
                val existing = operations.findByMonth(month)
                val suggestion = existing?.amountCents ?: operations.suggestedAmountFor(month)
                MonthOpeningState(validateMonth(month), existing?.amountCents?.let(::formatAmount) ?: suggestion?.let(::formatAmount) ?: "", suggestion)
            }.onSuccess { _opening.value = it }
                .onFailure { _opening.value = MonthOpeningState(month, error = it.message ?: "Mês inválido") }
        }
    }

    fun saveMonth(month: String, amount: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { operations.save(month, parseBudgetCents(amount)) }
                .onSuccess { _opening.value = null; refresh(); onComplete() }
                .onFailure { _opening.value = (_opening.value ?: MonthOpeningState(month)).copy(error = it.message ?: "Orçamento inválido") }
        }
    }

    fun closeOpening() { _opening.value = null }

    private fun parseBudgetCents(input: String): Long {
        val normalized = input.trim().replace(',', '.')
        return try {
            java.math.BigDecimal(normalized).setScale(2, java.math.RoundingMode.UNNECESSARY).movePointRight(2).longValueExact().also(::validateBudgetAmount)
        } catch (_: Exception) { throw DomainError.NegativeBudget }
    }

    private fun formatAmount(cents: Long): String = java.math.BigDecimal.valueOf(cents, 2).toPlainString()

    class Factory(private val database: BudgetDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BudgetViewModel(database) as T
    }
}
