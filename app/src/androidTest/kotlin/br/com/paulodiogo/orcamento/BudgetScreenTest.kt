package br.com.paulodiogo.orcamento

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.BudgetViewModel
import br.com.paulodiogo.orcamento.domain.BudgetOperations
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetScreenTest {
    @get:Rule val composeRule = createComposeRule()
    private lateinit var database: BudgetDatabase
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        viewModel = BudgetViewModel(database)
    }

    @After fun tearDown() = database.close()

    @Test
    fun exposesMonthOpeningAndEmptyHistoryState() {
        composeRule.setContent { BudgetScreen(viewModel) {} }
        composeRule.onNodeWithText("Orçamentos").assertTextContains("Orçamentos")
        composeRule.onNodeWithText("Abrir mês").assertTextContains("Abrir mês")
        composeRule.onNodeWithText("Nenhum mês configurado").assertTextContains("Nenhum mês configurado")
    }

    @Test
    fun budgetDialogFormatsAndPersistsDigitInputAsCents() {
        composeRule.setContent { BudgetScreen(viewModel) {} }
        composeRule.onNodeWithText("Abrir mês").performClick()
        val amountField = composeRule.onNodeWithTag("moneyTextField")
        amountField.performTextInput("12345")
        amountField.assertTextContains("123,45")
        composeRule.onNodeWithText("Confirmar").performClick()
        composeRule.waitUntil(5_000) {
            runBlocking { BudgetOperations(database).findByMonth("2026-10")?.amountCents == 12_345L }
        }

        assertEquals(12_345L, runBlocking { BudgetOperations(database).findByMonth("2026-10")?.amountCents })
    }
}
