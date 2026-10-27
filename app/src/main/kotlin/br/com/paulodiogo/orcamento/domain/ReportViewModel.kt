package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.TagExpenseTotal
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface ReportState {
    data object Idle : ReportState
    data object Loading : ReportState
    data class Content(
        val month: String,
        val budgetCents: Long?,
        val execution: BudgetExecution,
        val tagTotals: List<TagExpenseTotal>,
    ) : ReportState
    data object Empty : ReportState
    data class Error(val message: String) : ReportState
}

class ReportViewModel(private val database: BudgetDatabase) : ViewModel() {
    private val _state = MutableStateFlow<ReportState>(ReportState.Idle)
    val state: StateFlow<ReportState> = _state.asStateFlow()
    private var reportJob: Job? = null

    fun load(month: String) {
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            _state.value = ReportState.Loading
            database.expenseDao().observeAll().collectLatest {
                loadSnapshot(month)
            }
        }
    }

    private suspend fun loadSnapshot(month: String) {
        runCatching {
            validateMonth(month)
            val selectedMonth = YearMonth.parse(month)
            val startDate = selectedMonth.atDay(1).toString()
            val endDate = selectedMonth.atEndOfMonth().toString()
            val spent = database.expenseDao().sumAmountBetween(startDate, endDate)
            val budget = database.budgetDao().findByMonth(month)?.amountCents
            val execution = calculateBudgetExecution(budget ?: 0L, spent)
            val tags = database.expenseDao().sumAmountByTagBetween(startDate, endDate)
            ReportState.Content(month, budget, execution, tags)
        }.onSuccess { result ->
            _state.value = result
        }.onFailure { error ->
            _state.value = ReportState.Error(error.message ?: "Não foi possível carregar o relatório")
        }
    }

    private fun validateMonth(month: String) {
        br.com.paulodiogo.orcamento.domain.validateMonth(month)
        runCatching { YearMonth.parse(month) }
            .onFailure { throw DomainError.InvalidDate }
    }

    class Factory(private val database: BudgetDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportViewModel(database) as T
    }
}
