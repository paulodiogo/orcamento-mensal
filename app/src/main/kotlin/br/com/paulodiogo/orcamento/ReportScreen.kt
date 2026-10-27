package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun ReportScreen(viewModel: ReportViewModel, onBack: () -> Unit) {
    var month by rememberSaveable { mutableStateOf("2026-10") }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(month) { viewModel.load(month) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Relatórios", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onBack) { Text("Lançamentos") }
        }
        OutlinedTextField(
            value = month,
            onValueChange = { month = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mês (AAAA-MM)") },
            singleLine = true,
        )
        when (val current = state) {
            ReportState.Idle, ReportState.Loading -> CircularProgressIndicator()
            ReportState.Empty -> Text("Nenhum dado no período")
            is ReportState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ReportState.Content -> {
                Text("Orçamento: ${current.budgetCents?.let(::formatCents) ?: "não configurado"}")
                Text("Total gasto: ${formatCents(current.execution.spentCents)}")
                Text("Saldo: ${formatCents(current.execution.availableCents)}")
                Text("Execução: ${current.execution.percentage?.let { formatPercentage(it) } ?: "indisponível"}")
                Text("Por tag", style = MaterialTheme.typography.titleMedium)
                if (current.tagTotals.isEmpty()) {
                    Text("Nenhuma tag no período")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.tagTotals, key = { it.tagId }) { total ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(total.tagName)
                                Text(formatCents(total.amountCents))
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
