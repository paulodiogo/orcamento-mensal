package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.paulodiogo.orcamento.domain.ReportState
import br.com.paulodiogo.orcamento.domain.ReportViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import java.time.YearMonth

@Composable
fun ReportScreen(viewModel: ReportViewModel, contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(), onBack: () -> Unit = {}) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(month) { viewModel.load(month) }

    ScreenContent(contentPadding, horizontalPadding = 16.dp) {
        ScreenTitle("Relatórios", "Entenda para onde seu dinheiro está indo")
        MonthField(value = month, onValueChange = { month = it }, modifier = Modifier.fillMaxWidth(), label = "Mês")
        when (val current = state) {
            ReportState.Idle, ReportState.Loading -> CircularProgressIndicator()
            ReportState.Empty -> Text("Nenhum dado no período")
            is ReportState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ReportState.Content -> {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Saldo disponível", style = MaterialTheme.typography.labelLarge)
                        Text(formatCents(current.execution.availableCents), style = MaterialTheme.typography.headlineMedium)
                        Text("Orçamento: ${current.budgetCents?.let(::formatCents) ?: "não configurado"}")
                        Text("Gasto: ${formatCents(current.execution.spentCents)}")
                        Text("Consumo: ${current.execution.percentage?.let { formatPercentage(it) } ?: "indisponível"}")
                        current.execution.percentage?.let { LinearProgressIndicator({ (it.toDouble() / 100.0).coerceIn(0.0, 1.0).toFloat() }, Modifier.fillMaxWidth()) }
                    }
                }
                Text("Distribuição por tag", style = MaterialTheme.typography.titleMedium)
                if (current.tagTotals.isEmpty()) {
                    Text("Nenhuma tag no período")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.tagTotals, key = { it.tagId }) { total ->
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(total.tagName)
                                    Text(formatCents(total.amountCents), style = MaterialTheme.typography.titleSmall)
                                }
                                val totalSpent = current.execution.spentCents.coerceAtLeast(1L)
                                LinearProgressIndicator({ (total.amountCents.toDouble() / totalSpent).coerceIn(0.0, 1.0).toFloat() }, Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCents(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        .format(BigDecimal.valueOf(cents).movePointLeft(2))

private fun formatPercentage(value: BigDecimal): String = "$value%"
