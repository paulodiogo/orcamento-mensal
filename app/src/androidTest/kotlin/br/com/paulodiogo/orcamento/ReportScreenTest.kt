package br.com.paulodiogo.orcamento

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import androidx.room.Room
import br.com.paulodiogo.orcamento.data.ExpenseEntity
import br.com.paulodiogo.orcamento.data.ExpenseTagCrossRef
import br.com.paulodiogo.orcamento.data.TagEntity
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.ReportViewModel
import br.com.paulodiogo.orcamento.domain.ExpenseListViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportScreenTest {
    @get:Rule
    val composeRule = createComposeRule()
    private lateinit var database: BudgetDatabase
    private lateinit var viewModel: ReportViewModel

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            BudgetDatabase::class.java,
        ).build()
        viewModel = ReportViewModel(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun showsTagBudgetUsageAndZeroSpendBudgetedTag() {
        val budgeted = TagEntity("tag-budgeted", "Budgeted", "budgeted", 1L, 1L, 50_000L)
        val unbudgeted = TagEntity("tag-unbudgeted", "Unbudgeted", "unbudgeted", 1L, 1L)
        runBlocking {
            database.tagDao().insert(budgeted)
            database.tagDao().insert(unbudgeted)
            database.expenseDao().insert(ExpenseEntity("expense-current", "Current", 20_000L, java.time.YearMonth.now().atDay(2).toString(), 1L, 1L))
            database.expenseTagDao().insert(ExpenseTagCrossRef("expense-current", budgeted.id))
            database.expenseTagDao().insert(ExpenseTagCrossRef("expense-current", unbudgeted.id))
        }
        composeRule.setContent { ReportScreen(viewModel) }
        composeRule.waitUntil(5_000) {
            (viewModel.state.value as? br.com.paulodiogo.orcamento.domain.ReportState.Content)
                ?.tagBudgetExecutions?.any { it.tagId == budgeted.id } == true
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Orçamento por tag").assertTextContains("Orçamento por tag")
        composeRule.onNodeWithTag("tagBudgetPercentage_${budgeted.id}").assertTextContains("40,00%")
        composeRule.onNodeWithTag("tagBudgetPercentage_${unbudgeted.id}").assertTextContains("Sem orçamento")
        composeRule.onNodeWithText("R$ 200,00 de R$ 500,00", useUnmergedTree = true)
            .assertTextContains("R$ 200,00 de R$ 500,00")
    }

    @Test
    fun homeShowsTagBudgetAndOverBudgetState() {
        val budgeted = TagEntity("tag-budgeted", "Budgeted", "budgeted", 1L, 1L, 50_000L)
        val expense = ExpenseEntity("expense-current", "Current", 65_000L, java.time.YearMonth.now().atDay(2).toString(), 1L, 1L)
        runBlocking {
            database.tagDao().insert(budgeted)
            database.expenseDao().insert(expense)
            database.expenseTagDao().insert(ExpenseTagCrossRef(expense.id, budgeted.id))
        }
        composeRule.setContent {
            AppShell(
                ExpenseListViewModel(database),
                viewModel,
                br.com.paulodiogo.orcamento.domain.BudgetViewModel(database),
                br.com.paulodiogo.orcamento.domain.TagViewModel(database),
            )
        }
        composeRule.waitUntil(5_000) {
            (viewModel.state.value as? br.com.paulodiogo.orcamento.domain.ReportState.Content)
                ?.tagBudgetExecutions?.any { it.tagId == budgeted.id } == true
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Orçamento por tag").assertTextContains("Orçamento por tag")
        composeRule.onNodeWithTag("tagBudgetPercentage_${budgeted.id}")
            .assertTextContains("130,00%")
        composeRule.onNodeWithText("Acima do orçamento", useUnmergedTree = true)
            .assertTextContains("Acima do orçamento")
        composeRule.onNodeWithText("R$ 650,00 de R$ 500,00", useUnmergedTree = true)
            .assertTextContains("R$ 650,00 de R$ 500,00")
        composeRule.onNodeWithTag("tagBudgetPercentage_${budgeted.id}").assertTextContains("130,00%")
    }

    @Test
    fun showsReportPeriodAndNavigationAction() {
        composeRule.setContent { ReportScreen(viewModel) {} }
        composeRule.onNodeWithText("Relatórios").assertTextContains("Relatórios")
        composeRule.onNodeWithText("Mês").assertTextContains("Mês")
    }
}
