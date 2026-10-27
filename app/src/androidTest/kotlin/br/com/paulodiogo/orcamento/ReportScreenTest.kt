package br.com.paulodiogo.orcamento

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.ReportViewModel
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
    fun showsReportPeriodAndNavigationAction() {
        composeRule.setContent { ReportScreen(viewModel) {} }
        composeRule.onNodeWithText("Relatórios").assertTextContains("Relatórios")
        composeRule.onNodeWithText("Mês").assertTextContains("Mês")
    }
}
