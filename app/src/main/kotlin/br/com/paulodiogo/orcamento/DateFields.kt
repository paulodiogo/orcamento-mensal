package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("pt-BR"))

internal fun todayIsoDate(): String = LocalDate.now().format(isoFormatter)

internal fun parseIsoDate(value: String): LocalDate? = runCatching { LocalDate.parse(value, isoFormatter) }.getOrNull()

internal fun formatDisplayDate(value: String): String = parseIsoDate(value)?.format(displayFormatter).orEmpty()

private fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun pickerMillisToDate(value: Long): LocalDate = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowEmpty: Boolean = false,
    testTag: String? = null,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val displayValue = formatDisplayDate(value)
    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        modifier = modifier.clickable { pickerOpen = true }.then(if (testTag == null) Modifier else Modifier.semantics { this.testTag = testTag }),
        label = { Text(label) },
        placeholder = { if (allowEmpty) Text("dd/MM/aaaa") },
        readOnly = true,
        singleLine = true,
        trailingIcon = { TextButton(onClick = { pickerOpen = true }) { Text("📅") } },
    )
    if (pickerOpen) {
        val selectedDate = parseIsoDate(value)
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate?.toPickerMillis() ?: LocalDate.now().toPickerMillis())
        DatePickerDialog(
            onDismissRequest = { pickerOpen = false },
            confirmButton = {
                Button(onClick = {
                    state.selectedDateMillis?.let { onValueChange(pickerMillisToDate(it).format(isoFormatter)) }
                    pickerOpen = false
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { pickerOpen = false }) { Text("Cancelar") } },
        ) { DatePicker(state = state) }
    }
}

@Composable
internal fun MonthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.clickable { pickerOpen = true },
        label = { Text(label) },
        readOnly = true,
        singleLine = true,
        trailingIcon = { TextButton(onClick = { pickerOpen = true }) { Text("📅") } },
    )
    if (pickerOpen) {
        MonthPickerDialog(
            value = value,
            onConfirm = { onValueChange(it); pickerOpen = false },
            onDismiss = { pickerOpen = false },
        )
    }
}

@Composable
private fun MonthPickerDialog(value: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var month by remember(value) { mutableStateOf(runCatching { YearMonth.parse(value) }.getOrDefault(YearMonth.now())) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar mês") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { month = month.minusMonths(1) }) { Text("‹") }
                Text(month.format(monthFormatter).replaceFirstChar { it.titlecase(Locale.forLanguageTag("pt-BR")) }, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { month = month.plusMonths(1) }) { Text("›") }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(month.toString()) }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
