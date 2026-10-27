package br.com.paulodiogo.orcamento

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.domain.BudgetState
import br.com.paulodiogo.orcamento.domain.BudgetViewModel
import br.com.paulodiogo.orcamento.domain.MonthOpeningState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetScreen(viewModel: BudgetViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val opening by viewModel.opening.collectAsState()
    var month by rememberSaveable { mutableStateOf("2026-10") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Orçamentos", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Lançamentos") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(month, { month = it }, Modifier.weight(1f), label = { Text("Mês (AAAA-MM)") }, singleLine = true)
            Button(onClick = { viewModel.openMonth(month) }) { Text("Abrir mês") }
        }
        when (val current = state) {
            BudgetState.Loading -> CircularProgressIndicator()
            BudgetState.Empty -> Text("Nenhum mês configurado")
            is BudgetState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is BudgetState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current.budgets, key = { it.month }) { budget ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(budget.month)
                        Text(formatCents(budget.amountCents))
                        TextButton(onClick = { viewModel.openMonth(budget.month) }) { Text("Editar") }
                    }
                }
            }
        }
    }
    opening?.let { stateValue -> MonthOpeningDialog(stateValue, viewModel) }
}

@Composable
private fun MonthOpeningDialog(state: MonthOpeningState, viewModel: BudgetViewModel) {
    var amount by rememberSaveable(state.month, state.suggested) { mutableStateOf(state.amount) }
    AlertDialog(
        onDismissRequest = viewModel::closeOpening,
        title = { Text("Abrir mês ${state.month}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.suggested?.let { Text("Sugestão do mês anterior: ${formatCents(it)}") }
                OutlinedTextField(amount, { amount = it }, label = { Text("Orçamento") }, singleLine = true)
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveMonth(state.month, amount) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = viewModel::closeOpening) { Text("Cancelar") } },
    )
}

private fun formatCents(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(BigDecimal.valueOf(cents, 2))
