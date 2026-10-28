package com.viniciusrio.dengo

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import com.viniciusrio.dengo.ui.mural.MuralViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadFailureComposerTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class ControlledRepository(private val fake: FakeCoupleRepository = FakeCoupleRepository()) : CoupleRepository by fake {
        override val state = MutableStateFlow(fake.state.value)
        val requestResult = CompletableDeferred<WriteResult<CareRequest>>()
        val textResult = CompletableDeferred<WriteResult<MuralNote>>()
        val drawingResult = CompletableDeferred<WriteResult<MuralNote>>()
        var retryCalls = 0

        override fun retryRead() {
            retryCalls++
            state.value = fake.state.value
        }
        override suspend fun createRequest(type: com.viniciusrio.dengo.model.CareRequestType, message: String?) = requestResult.await()
        override suspend fun createMuralNote(authorId: PartnerId, text: String) = textResult.await()
        override suspend fun createMuralDrawing(authorId: PartnerId, aspectRatio: Float, strokes: List<DrawingStroke>) = drawingResult.await()
    }

    private fun showApp(repository: ControlledRepository): Pair<HomeViewModel, MuralViewModel> {
        val home = HomeViewModel(repository)
        val mural = MuralViewModel(repository)
        composeRule.setContent {
            DengoApp(home, ViniciusHomeViewModel(repository), HistoryViewModel(repository), mural)
        }
        return home to mural
    }

    private fun assertVisibleText(text: String) {
        val matches = composeRule.onAllNodesWithText(text)
        matches[matches.fetchSemanticsNodes().lastIndex].assertIsDisplayed()
    }

    @Test fun otherDialogKeepsItsTextVisibleThroughLoadingAndReadError() {
        val repository = ControlledRepository()
        showApp(repository)
        composeRule.onNodeWithText("Outro pedido").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Preciso de um abraço")
        composeRule.onNodeWithText("Enviar").performClick()

        composeRule.runOnIdle { repository.state.value = RepositoryReadState.Loading }
        assertVisibleText("Preciso de um abraço")
        assertVisibleText(composeRule.activity.getString(R.string.read_stale_loading))

        composeRule.runOnIdle { repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read")) }
        assertVisibleText("Preciso de um abraço")
        assertVisibleText(composeRule.activity.getString(R.string.read_stale_failed))
        assertVisibleText("Tentar novamente")
        composeRule.onNodeWithText("Tentar novamente").performClick()
        assertVisibleText("Preciso de um abraço")
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.read_stale_failed)).assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(1, repository.retryCalls) }
    }

    @Test fun muralTextSheetKeepsDraftVisibleThroughLoadingAndReadError() {
        val repository = ControlledRepository()
        showApp(repository)
        composeRule.onNodeWithText("Mural").performClick()
        composeRule.onNodeWithText("Deixar um recado").performClick()
        composeRule.onNodeWithText("Escrever").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Meu recado")
        composeRule.onNodeWithText("Deixar recado").performClick()

        composeRule.runOnIdle { repository.state.value = RepositoryReadState.Loading }
        assertVisibleText("Meu recado")
        assertVisibleText(composeRule.activity.getString(R.string.read_stale_loading))

        composeRule.runOnIdle { repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read")) }
        assertVisibleText("Meu recado")
        assertVisibleText(composeRule.activity.getString(R.string.read_stale_failed))
    }

    @Test fun muralDrawingSheetKeepsStrokesVisibleThroughLoadingAndReadError() {
        val repository = ControlledRepository()
        val (_, mural) = showApp(repository)
        composeRule.onNodeWithText("Mural").performClick()
        composeRule.runOnIdle {
            mural.openDrawing()
            mural.addStroke(listOf(DrawingPoint(0.5f, 0.5f)), 1f)
            mural.publishDrawing(PartnerId.LIDIANNE)
            repository.state.value = RepositoryReadState.Loading
        }

        composeRule.onNodeWithText("Desenhe seu recado").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Área para desenhar com o dedo").assertIsDisplayed()
        assertVisibleText(composeRule.activity.getString(R.string.read_stale_loading))

        composeRule.runOnIdle { repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read")) }
        composeRule.onNodeWithText("Desenhe seu recado").assertIsDisplayed()
        assertVisibleText(composeRule.activity.getString(R.string.read_stale_failed))
    }
}
