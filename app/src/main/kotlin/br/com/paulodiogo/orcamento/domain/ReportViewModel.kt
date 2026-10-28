package br.com.paulodiogo.orcamento.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.data.DatabaseOperationGate
import br.com.paulodiogo.orcamento.data.TagBudgetUsageRow
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin

sealed interface ReportState {
    data object Idle : ReportState
    data object Loading : ReportState
    data class Content(
        val month: String,
        val budgetCents: Long?,
        val execution: BudgetExecution,
        val tagBudgetExecutions: List<TagBudgetExecution>,
    ) : ReportState
    data object Empty : ReportState
    data class Error(val message: String) : ReportState
}

class ReportViewModel(private var database: BudgetDatabase) : ViewModel() {
    private val _state = MutableStateFlow<ReportState>(ReportState.Idle)
    val state: StateFlow<ReportState> = _state.asStateFlow()
    private var reportJob: Job? = null

    suspend fun pauseDatabaseObservers() {
        reportJob?.cancelAndJoin()
    }

    fun load(month: String) {
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            _state.value = ReportState.Loading
            runCatching { validateMonth(month); YearMonth.parse(month) }
                .onFailure { _state.value = ReportState.Error(it.message ?: "Mês inválido") }
                .getOrNull()
                ?.let { selectedMonth ->
                    val startDate = selectedMonth.atDay(1).toString()
                    val endDate = selectedMonth.atEndOfMonth().toString()
                    runCatching {
                        database.expenseDao().observeTagBudgetUsageBetween(startDate, endDate).collectLatest {
                            DatabaseOperationGate.read { loadSnapshot(month, it) }
                        }
                    }.onFailure { _state.value = ReportState.Error(it.message ?: "Não foi possível carregar o relatório") }
                }
        }
    }

    fun onDatabaseReplaced(database: BudgetDatabase) {
        this.database = database
        val month = (state.value as? ReportState.Content)?.month ?: YearMonth.now().toString()
        load(month)
    }

    private suspend fun loadSnapshot(month: String, tagRows: List<TagBudgetUsageRow>) {
        runCatching {
            validateMonth(month)
            val selectedMonth = YearMonth.parse(month)
            val startDate = selectedMonth.atDay(1).toString()
            val endDate = selectedMonth.atEndOfMonth().toString()
            val spent = database.expenseDao().sumAmountBetween(startDate, endDate)
            val budget = database.budgetDao().findByMonth(month)?.amountCents
            val execution = calculateBudgetExecution(budget ?: 0L, spent)
            val tags = calculateTagBudgetExecutions(tagRows)
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
