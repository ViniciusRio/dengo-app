package com.viniciusrio.dengo.ui.mural

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.theme.DengoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawingComposerSheetTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun assertSingleLine(color: String) {
        val layouts = mutableListOf<TextLayoutResult>()
        val node = composeRule.onNodeWithText(color, useUnmergedTree = true).fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action!!.invoke(layouts)
        assertEquals("$color quebrou em mais de uma linha", 1, layouts.single().lineCount)
    }

    @Test fun drawingComposerOpensWithActionsVisibleAndKeepsThemVisibleAcrossColors() {
        var composer by mutableStateOf(MuralComposerState(mode = ComposerMode.DRAWING))
        composeRule.setContent {
            DengoTheme {
                MuralScreen(
                    state = MuralUiState(),
                    composer = composer,
                    authorId = PartnerId.LIDIANNE,
                    onLeaveNote = { _, _ -> },
                    onOpenChooser = {},
                    onOpenText = {},
                    onOpenDrawing = {},
                    onSelectColor = { composer = composer.copy(selectedArgb = it) },
                    onStroke = { _, _, _ -> },
                    onUndo = {},
                    onClear = {},
                    onPublishDrawing = { false },
                    onRequestExit = { true },
                    onContinueDrawing = {},
                    onDiscard = {},
                )
            }
        }

        composeRule.onNodeWithText("Deixar recado").assertIsDisplayed()
        val initialTop = composeRule.onNodeWithText("Desenhe seu recado").fetchSemanticsNode().boundsInWindow.top
        listOf("Rosa", "Azul", "Escuro").forEach { color ->
            assertSingleLine(color)
        }
        listOf("Azul", "Escuro", "Rosa").forEach { color ->
            composeRule.onNodeWithText(color, substring = true).assertIsDisplayed().performClick()
            composeRule.onNodeWithText(color, substring = true).assertIsSelected()
            composeRule.onNodeWithText("Deixar recado").assertIsDisplayed()
            assertSingleLine(color)
            val top = composeRule.onNodeWithText("Desenhe seu recado").fetchSemanticsNode().boundsInWindow.top
            assertEquals("O sheet mudou de posição após selecionar $color", initialTop, top, 2f)
        }
    }
}
