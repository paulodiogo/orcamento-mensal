package br.com.paulodiogo.orcamento

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.BudgetViewModel
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
}
