package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Spacer(Modifier.height(12.dp))
        ScreenTitle("Orçamentos", "Planeje cada mês sem misturar seus dados")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Consultar outro mês", style = MaterialTheme.typography.labelLarge)
            TextButton(onClick = onBack) { Text("Lançamentos") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(month, { month = it }, Modifier.weight(1f), label = { Text("Mês (AAAA-MM)") }, singleLine = true)
            Button(onClick = { viewModel.openMonth(month) }) { Text("Abrir mês") }
        }
        when (val current = state) {
            BudgetState.Loading -> CircularProgressIndicator()
            BudgetState.Empty -> EmptyState("Nenhum mês configurado", "Abra o mês atual para definir seu primeiro orçamento.")
            is BudgetState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is BudgetState.Content -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(current.budgets, key = { it.month }) { budget ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(budget.month, style = MaterialTheme.typography.titleMedium)
                                Text(formatCents(budget.amountCents), style = MaterialTheme.typography.titleMedium)
                            }
                            Text("Orçamento confirmado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { viewModel.openMonth(budget.month) }) { Text("Editar orçamento") }
                        }
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
                state.suggested?.let {
                    Text("Sugestão do mês anterior", style = MaterialTheme.typography.labelLarge)
                    Text(formatCents(it), style = MaterialTheme.typography.headlineSmall)
                    Text("Revise o valor antes de confirmar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedTextField(amount, { amount = it }, label = { Text("Orçamento mensal") }, singleLine = true)
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = { viewModel.saveMonth(state.month, amount) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = viewModel::closeOpening) { Text("Cancelar") } },
    )
}

private fun formatCents(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(BigDecimal.valueOf(cents, 2))
