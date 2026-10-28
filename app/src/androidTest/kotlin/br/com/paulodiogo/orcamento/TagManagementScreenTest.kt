package br.com.paulodiogo.orcamento

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.paulodiogo.orcamento.data.BudgetDatabase
import br.com.paulodiogo.orcamento.domain.TagViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagManagementScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var database: BudgetDatabase
    private lateinit var viewModel: TagViewModel

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BudgetDatabase::class.java,
        ).build()
        viewModel = TagViewModel(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun createsTagWithoutBudgetUsingSemanticSelectors() {
        showScreen()
        openEditor()
        composeRule.onNodeWithTag("tagNameField").performTextInput("UI_TAG_SEM_ORCAMENTO")
        composeRule.onNodeWithTag("tagBudgetField").assertIsDisplayed()
        composeRule.onNodeWithText("Salvar").performClick()
        waitForTag("UI_TAG_SEM_ORCAMENTO")

        assertNull(runBlocking { database.tagDao().findByNormalizedName("ui_tag_sem_orcamento")?.budgetAmountCents })
        composeRule.onNodeWithText("UI_TAG_SEM_ORCAMENTO").assertIsDisplayed()
    }

    @Test
    fun createsAndEditsTagBudgetThroughTheForm() {
        showScreen()
        openEditor()
        composeRule.onNodeWithTag("tagNameField").performTextInput("UI_TAG_COM_ORCAMENTO")
        composeRule.onNodeWithTag("tagBudgetField").performTextInput("12345")
        composeRule.onNodeWithText("Salvar").performClick()
        waitForTag("UI_TAG_COM_ORCAMENTO")

        val created = runBlocking { database.tagDao().findByNormalizedName("ui_tag_com_orcamento")!! }
        assertEquals(12_345L, created.budgetAmountCents)

        composeRule.onNodeWithContentDescription("Editar tag UI_TAG_COM_ORCAMENTO").performClick()
        composeRule.onNodeWithTag("tagBudgetField").performTextClearance()
        composeRule.onNodeWithTag("tagBudgetField").performTextInput("67890")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitUntil(5_000) {
            runBlocking { database.tagDao().findById(created.id)?.budgetAmountCents == 67_890L }
        }
        assertEquals("UI_TAG_COM_ORCAMENTO", runBlocking { database.tagDao().findById(created.id)?.name })

        composeRule.onNodeWithContentDescription("Editar tag UI_TAG_COM_ORCAMENTO").performClick()
        composeRule.onNodeWithTag("tagBudgetField").performTextClearance()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitUntil(5_000) {
            runBlocking { database.tagDao().findById(created.id)?.budgetAmountCents == null }
        }
    }

    @Test
    fun showsAvailableBudgetOnlyForConfiguredTags() {
        showScreen()
        viewModel.save("UI_TAG_COM_SALDO", 10_000L)
        waitForTag("UI_TAG_COM_SALDO")
        composeRule.onNodeWithText("Disponível", substring = true).assertIsDisplayed()

        viewModel.save("UI_TAG_SEM_SALDO")
        waitForTag("UI_TAG_SEM_SALDO")
        composeRule.onNodeWithText("UI_TAG_SEM_SALDO").assertIsDisplayed()
        composeRule.onNodeWithText("Orçamento não configurado").assertIsDisplayed()
    }

    @Test
    fun exposesSpecificAccessibleActionDescriptions() {
        showScreen()
        viewModel.save("UI_TAG_ACESSIVEL", 1_000L)
        waitForTag("UI_TAG_ACESSIVEL")
        composeRule.onNodeWithContentDescription("Editar tag UI_TAG_ACESSIVEL").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Excluir tag UI_TAG_ACESSIVEL").assertIsDisplayed()
    }

    @Test
    fun rejectsBlankNameAndDuplicateWithoutCreatingAnotherTag() {
        showScreen()
        openEditor()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithTag("tagEditor").assertIsDisplayed()
        assertEquals(0, runBlocking { database.tagDao().count() })

        composeRule.onNodeWithTag("tagNameField").performTextInput("UI_TAG_DUPLICADA")
        composeRule.onNodeWithText("Salvar").performClick()
        waitForTag("UI_TAG_DUPLICADA")
        assertEquals(1, runBlocking { database.tagDao().count() })

        openEditor()
        composeRule.onNodeWithTag("tagNameField").performTextInput(" ui_tag_duplicada ")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithTag("tagEditor").assertIsDisplayed()
        assertEquals(1, runBlocking { database.tagDao().count() })
    }

    @Test
    fun cancelCreateAndEditDoesNotPersistChanges() {
        showScreen()
        openEditor()
        composeRule.onNodeWithTag("tagNameField").performTextInput("UI_TAG_CANCELADA")
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(0, runBlocking { database.tagDao().count() })

        viewModel.save("UI_TAG_ORIGINAL", 10_000L)
        waitForTag("UI_TAG_ORIGINAL")
        val original = runBlocking { database.tagDao().findByNormalizedName("ui_tag_original")!! }
        composeRule.onNodeWithContentDescription("Editar tag UI_TAG_ORIGINAL").performClick()
        composeRule.onNodeWithTag("tagNameField").performTextClearance()
        composeRule.onNodeWithTag("tagNameField").performTextInput("UI_TAG_ALTERADA")
        composeRule.onNodeWithTag("tagBudgetField").performTextClearance()
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(original, runBlocking { database.tagDao().findById(original.id) })
        composeRule.onNodeWithText("UI_TAG_ORIGINAL").assertIsDisplayed()
    }

    @Test
    fun deleteAndReopenPreservesCreatedTagAndBudgetBeforeDeletion() {
        showScreen()
        viewModel.save("UI_TAG_PERSISTENTE", 54_321L)
        waitForTag("UI_TAG_PERSISTENTE")
        val created = runBlocking { database.tagDao().findByNormalizedName("ui_tag_persistente")!! }

        composeRule.onNodeWithContentDescription("Editar tag UI_TAG_PERSISTENTE").performClick()
        composeRule.onNodeWithTag("tagBudgetField").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()
        assertEquals(54_321L, runBlocking { database.tagDao().findById(created.id)?.budgetAmountCents })

        composeRule.onNodeWithContentDescription("Excluir tag UI_TAG_PERSISTENTE").performClick()
        composeRule.onNodeWithTag("confirmDeleteTagButton").performClick()
        composeRule.waitUntil(5_000) { runBlocking { database.tagDao().findById(created.id) == null } }
        assertNull(runBlocking { database.tagDao().findById(created.id) })
    }

    private fun showScreen() {
        composeRule.setContent {
            TagManagementScreen(viewModel, PaddingValues())
        }
        composeRule.waitForIdle()
    }

    private fun openEditor() {
        composeRule.onNodeWithTag("createTagButton").performClick()
        composeRule.onNodeWithTag("tagEditor").assertIsDisplayed()
    }

    private fun waitForTag(name: String) {
        composeRule.waitUntil(5_000) {
            runBlocking { database.tagDao().findByNormalizedName(name.lowercase()) != null }
        }
        composeRule.onNodeWithText(name).assertIsDisplayed()
    }
}
