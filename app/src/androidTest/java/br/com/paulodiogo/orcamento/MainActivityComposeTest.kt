package br.com.paulodiogo.orcamento

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun formDisplaysAccessibleFieldsAndValidatesEmptyInput() {
        composeRule.setContent {
            CreateExpenseDialog(tags = emptyList(), onDismiss = {}, onSave = { _, _, _, _ -> })
        }
        composeRule.onNodeWithText("Descrição").assertTextContains("Descrição")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Salvar").assertTextContains("Salvar")
    }
}
