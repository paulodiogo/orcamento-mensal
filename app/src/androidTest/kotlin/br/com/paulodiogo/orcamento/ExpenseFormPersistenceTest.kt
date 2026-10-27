package br.com.paulodiogo.orcamento

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.ExpenseListViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseFormPersistenceTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var database: BudgetDatabase
    private lateinit var viewModel: ExpenseListViewModel
    private val viewModelStore = ViewModelStore()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BudgetDatabase::class.java,
        ).build()
        viewModel = ExpenseListViewModel(database)
        viewModelStore.put("expense-view-model", viewModel)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun savingExpenseFormPersistsDigitValueAsCentsAndIgnoresDuplicateRequest() {
        composeRule.setContent {
            CreateExpenseDialog(
                tags = emptyList(),
                onDismiss = {},
                onSave = { description, cents, date, tagIds ->
                    viewModel.create(description, cents, date, tagIds)
                    viewModel.create(description, cents, date, tagIds)
                },
            )
        }

        composeRule.onNodeWithText("Descrição").performTextInput("Compra")
        composeRule.onNodeWithTag("moneyTextField").performTextInput("12345")
        composeRule.onNodeWithTag("expenseDateField").assertTextContains(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitUntil(5_000) {
            runBlocking { database.expenseDao().findAll().isNotEmpty() }
        }

        val expenses = runBlocking { database.expenseDao().findAll() }
        assertEquals(1, expenses.size)
        assertEquals(12_345L, expenses.single().amountCents)
    }
}
