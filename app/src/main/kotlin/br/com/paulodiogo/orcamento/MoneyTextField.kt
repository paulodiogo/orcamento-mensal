package br.com.paulodiogo.orcamento

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import br.com.paulodiogo.orcamento.domain.InvalidMoneyInput
import br.com.paulodiogo.orcamento.domain.MoneyInputFailure
import br.com.paulodiogo.orcamento.domain.MAX_MONEY_DIGITS
import br.com.paulodiogo.orcamento.domain.formatMoneyDigits
import br.com.paulodiogo.orcamento.domain.normalizeMoneyDigits
import br.com.paulodiogo.orcamento.domain.parseMoneyDigitsToCents
import br.com.paulodiogo.orcamento.domain.sanitizeMoneyDigits

@Composable
fun MoneyTextField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    error: String? = null,
    onInputError: (String?) -> Unit = {},
) {
    val emptyInputMessage = stringResource(R.string.money_field_no_digits)
    val invalidInputMessage = stringResource(R.string.money_field_invalid)
    val tooLargeMessage = stringResource(R.string.money_field_too_large)
    val currencyPrefix = stringResource(R.string.money_field_prefix)
    var fieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(formattedValue(digits))
    }

    SideEffect {
        val expected = formatMoneyDigits(digits)
        if (fieldValue.text != expected) fieldValue = formattedValue(digits)
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { proposed ->
            val currentText = formatMoneyDigits(digits)
            if (proposed.text == currentText) {
                fieldValue = proposed
                return@OutlinedTextField
            }
            val extracted = sanitizeMoneyDigits(proposed.text)
            if (extracted.isEmpty() && proposed.text.isNotEmpty()) {
                onInputError(emptyInputMessage)
                return@OutlinedTextField
            }
            val parsed = try {
                if (extracted.length > MAX_MONEY_DIGITS) throw InvalidMoneyInput(MoneyInputFailure.TOO_MANY_DIGITS)
                if (extracted.isEmpty()) "" else {
                    parseMoneyDigitsToCents(extracted)
                    normalizeMoneyDigits(extracted)
                }
            } catch (exception: InvalidMoneyInput) {
                onInputError(exception.toDisplayMessage(emptyInputMessage, invalidInputMessage, tooLargeMessage))
                return@OutlinedTextField
            } catch (exception: IllegalArgumentException) {
                onInputError(invalidInputMessage)
                return@OutlinedTextField
            }

            val nextDigits = when {
                parsed == normalizeMoneyDigits(digits) && proposed.text.length < currentText.length ->
                    removeAdjacentDigit(digits, currentText, fieldValue.selection, proposed.selection)
                parsed == normalizeMoneyDigits(digits) -> digits
                else -> parsed
            }
            val nextText = formatMoneyDigits(nextDigits)
            val insertedDigitsBeforeCursor = countDigits(proposed.text, proposed.selection.start)
            val padding = if (proposed.text == formatMoneyDigits(parsed)) {
                (3 - parsed.length).coerceAtLeast(0)
            }
            else 0
            val rawCursor = (insertedDigitsBeforeCursor - padding).coerceIn(0, nextDigits.length)
            val cursor = displayOffsetForDigitIndex(nextDigits, rawCursor)
            fieldValue = TextFieldValue(nextText, TextRange(cursor))
            onDigitsChange(nextDigits)
            onInputError(null)
        },
        modifier = modifier.testTag("moneyTextField"),
        label = { Text(label) },
        prefix = { Text(currencyPrefix) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

fun InvalidMoneyInput.toDisplayMessage(empty: String, invalid: String, tooLarge: String): String =
    when (reason) {
        MoneyInputFailure.EMPTY -> empty
        MoneyInputFailure.TOO_MANY_DIGITS, MoneyInputFailure.OUT_OF_RANGE -> tooLarge
        MoneyInputFailure.NON_DIGIT, MoneyInputFailure.NEGATIVE -> invalid
    }

private fun formattedValue(digits: String): TextFieldValue {
    val text = formatMoneyDigits(digits)
    return TextFieldValue(text, TextRange(text.length))
}

private fun countDigits(text: String, offset: Int): Int =
    text.take(offset.coerceIn(0, text.length)).count { it in '0'..'9' }

private fun displayOffsetForDigitIndex(digits: String, digitIndex: Int): Int {
    val text = formatMoneyDigits(digits)
    if (digits.isEmpty()) return 0
    val padding = (3 - normalizeMoneyDigits(digits).length).coerceAtLeast(0)
    val targetDigit = digitIndex + padding
    if (targetDigit <= 0) return 0
    var seen = 0
    text.forEachIndexed { index, char ->
        if (char in '0'..'9') {
            seen++
            if (seen == targetDigit) return index + 1
        }
    }
    return text.length
}

private fun removeAdjacentDigit(
    digits: String,
    currentText: String,
    oldSelection: TextRange,
    newSelection: TextRange,
): String {
    val normalized = normalizeMoneyDigits(digits)
    if (normalized.isEmpty() || oldSelection.collapsed.not() || newSelection.collapsed.not()) return digits
    val oldOffset = oldSelection.start
    val newOffset = newSelection.start
    val padding = (3 - normalized.length).coerceAtLeast(0)
    val logicalPosition = (countDigits(currentText, oldOffset) - padding).coerceIn(0, normalized.length)
    val index = when {
        newOffset < oldOffset -> logicalPosition - 1
        newOffset > oldOffset -> logicalPosition
        else -> return digits
    }
    if (index !in normalized.indices) return digits
    return (normalized.take(index) + normalized.drop(index + 1)).ifEmpty { "" }
}
