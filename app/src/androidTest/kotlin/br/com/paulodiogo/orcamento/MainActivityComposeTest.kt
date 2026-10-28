package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.com.paulodiogo.orcamento.domain.parseMoneyDigitsToCents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class MainActivityComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun backupScreenCanOfferExistingFolderWithoutRequiringCreateBackup() {
        composeRule.setContent {
            BackupScreen(
                padding = androidx.compose.foundation.layout.PaddingValues(),
                busy = false,
                message = null,
                lastBackupName = null,
                onBack = {},
                folderSelected = false,
                backups = emptyList(),
                selectedGeneration = null,
                onCreate = {},
                onRestore = {},
                onChooseBackupFolder = {},
                onSelectBackup = {},
            )
        }
        composeRule.onNodeWithText("Selecionar pasta existente para restaurar").assertTextContains("Selecionar pasta existente para restaurar")
        composeRule.onNodeWithText("Restaurar backup").assertIsNotEnabled()
    }

    @Test
    fun backupScreenOffersOnlyCreateAndRestoreActions() {
        composeRule.setContent {
            BackupScreen(
                padding = androidx.compose.foundation.layout.PaddingValues(),
                busy = false,
                message = null,
                lastBackupName = "backup.db",
                onBack = {},
                folderSelected = true,
                backups = emptyList(),
                selectedGeneration = null,
                onCreate = {},
                onRestore = {},
                onChooseBackupFolder = {},
                onSelectBackup = {},
            )
        }
        composeRule.onNodeWithText("Criar backup").assertTextContains("Criar backup")
        composeRule.onNodeWithText("Restaurar backup").assertTextContains("Restaurar backup")
        org.junit.Assert.assertEquals(
            0,
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Selecionar arquivo SQLite") or androidx.compose.ui.test.hasText("Selecionar manifesto do backup"), useUnmergedTree = true)
                .fetchSemanticsNodes().size,
        )
    }

    @Test
    fun formDisplaysAccessibleFieldsAndValidatesEmptyInput() {
        composeRule.setContent {
            CreateExpenseDialog(tags = emptyList(), onDismiss = {}, onSave = { _, _, _, _ -> })
        }
        composeRule.onNodeWithText("Descrição").assertTextContains("Descrição")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Salvar").assertTextContains("Salvar")
    }

    @Test
    fun moneyFieldFormatsDigitsAsBrazilianCentsWhileTyping() {
        composeRule.setContent {
            var digits by remember { mutableStateOf("") }
            MoneyTextField(digits = digits, onDigitsChange = { digits = it }, label = "Valor")
        }

        listOf(
            "1" to "0,01",
            "2" to "0,12",
            "3" to "1,23",
            "4" to "12,34",
            "5" to "123,45",
            "6" to "1.234,56",
        ).forEach { (digit, expected) ->
            composeRule.onNodeWithTag("moneyTextField").performTextInput(digit)
            composeRule.onNodeWithTag("moneyTextField").assertTextContains(expected)
        }
    }

    @Test
    fun moneyFieldSupportsBackspaceAndEditingInTheMiddle() {
        composeRule.setContent {
            var digits by remember { mutableStateOf("") }
            MoneyTextField(digits = digits, onDigitsChange = { digits = it }, label = "Valor")
        }
        val field = composeRule.onNodeWithTag("moneyTextField")
        field.performTextInput("123456")
        field.assertTextContains("1.234,56")
        field.performKeyInput { pressKey(Key.Backspace) }
        field.assertTextContains("123,45")
        field.performTextInputSelection(TextRange(2))
        field.performTextInput("9")
        field.assertTextContains("1.293,45")
        composeRule.waitForIdle()
        assertEquals(TextRange(4), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
    }

    @Test
    fun moneyFieldSanitizesPastedTextAndReportsPasteWithoutDigits() {
        composeRule.setContent {
            var digits by remember { mutableStateOf("") }
            var error by remember { mutableStateOf<String?>(null) }
            MoneyTextField(
                digits = digits,
                onDigitsChange = { digits = it },
                label = "Valor",
                error = error,
                onInputError = { error = it },
            )
        }
        val field = composeRule.onNodeWithTag("moneyTextField")
        field.performTextInput("R$ 1.234,56")
        field.assertTextContains("1.234,56")
        field.performTextInputSelection(TextRange(0, 8))
        field.performTextInput("9223372036854775808")
        field.assertTextContains("O valor excede o limite permitido.")
        field.assertTextContains("1.234,56")
        field.performTextInputSelection(TextRange(0, 8))
        field.performTextInput("abc -., ")
        field.assertTextContains("Digite ou cole pelo menos um algarismo.")
        field.assertTextContains("1.234,56")
        field.performTextInputSelection(TextRange(0, 8))
        field.performTextInput("1234")
        field.assertTextContains("12,34")
        composeRule.waitForIdle()
        assertFalse(
            field.fetchSemanticsNode().config[SemanticsProperties.Text]
                .any { it.text.contains("Digite ou cole pelo menos um algarismo.") },
        )
    }

    @Test
    fun expenseFormSavesTheExactCentsAndRejectsZero() {
        var savedCents: Long? = null
        composeRule.setContent {
            CreateExpenseDialog(
                tags = emptyList(),
                onDismiss = {},
                onSave = { _, cents, _, _ -> savedCents = cents },
            )
        }

        val hoje = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        composeRule.onNodeWithText("Descrição").performTextInput("Compra")
        composeRule.onNodeWithTag("moneyTextField").performTextInput("12345")
        composeRule.onNodeWithTag("expenseDateField").assertTextContains(hoje)
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.runOnIdle { assertEquals(12_345L, savedCents) }

        composeRule.onNodeWithTag("moneyTextField").performTextInputSelection(TextRange(0, 6))
        composeRule.onNodeWithTag("moneyTextField").performTextInput("0")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithTag("moneyTextField").assertTextContains("O valor da despesa deve ser maior que zero.")
        composeRule.runOnIdle { assertEquals(12_345L, savedCents) }
    }

    @Test
    fun centsParserUsesZeroForBudgetButRejectsItForExpenses() {
        assertEquals(0L, parseMoneyDigitsToCents("0"))
        org.junit.Assert.assertThrows(br.com.paulodiogo.orcamento.domain.DomainError.InvalidAmount::class.java) {
            br.com.paulodiogo.orcamento.domain.validateExpenseAmount(0L)
        }
    }

    @Test
    fun editingExpenseReopensWithThePersistedCentsFormatted() {
        composeRule.setContent {
            CreateExpenseDialog(
                tags = emptyList(),
                initialDescription = "Compra",
                initialAmountCents = 12_345L,
                initialOccurredOn = "2026-10-27",
                onDismiss = {},
                onSave = { _, _, _, _ -> },
            )
        }

        composeRule.onNodeWithTag("moneyTextField").assertTextContains("123,45")
        composeRule.onNodeWithTag("moneyTextField").performTextInputSelection(TextRange(0, 6))
        composeRule.onNodeWithTag("moneyTextField").performTextInput("123456")
        composeRule.onNodeWithTag("moneyTextField").assertTextContains("1.234,56")
    }

    @Test
    fun moneyFieldRetainsDigitsAfterLosingAndRegainingFocus() {
        composeRule.setContent {
            var digits by remember { mutableStateOf("") }
            Column {
                MoneyTextField(digits = digits, onDigitsChange = { digits = it }, label = "Valor")
                Button(onClick = {}) { Text("Outro controle") }
            }
        }
        val field = composeRule.onNodeWithTag("moneyTextField")
        field.performTextInput("123")
        composeRule.onNodeWithText("Outro controle").performClick()
        field.performClick()
        field.assertTextContains("1,23")
    }

    @Test
    fun expenseSaveActionIsDisabledWhileSaving() {
        composeRule.setContent {
            CreateExpenseDialog(
                tags = emptyList(),
                saving = true,
                onDismiss = {},
                onSave = { _, _, _, _ -> },
            )
        }

        composeRule.onNodeWithText("Salvando...").assertIsNotEnabled()
    }
}
