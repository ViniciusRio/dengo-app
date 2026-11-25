package com.viniciusrio.dengo.ui.pairing

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.ui.theme.DengoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PairingScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(state: PairingUiState, onCode: (String) -> Unit = {}, onSubmit: () -> Unit = {}) {
        composeRule.setContent { DengoTheme { PairingScreen(state, onCode, onSubmit) } }
    }

    @Test fun typingAndConfirmingReachTheViewModel() {
        var typed = ""
        var submitted = false
        show(PairingUiState(code = "ABCD2345"), { typed = it }, { submitted = true })

        composeRule.onNode(hasSetTextAction()).performTextInput("9")
        composeRule.onNodeWithText("Vincular").performClick()

        assertTrue(typed.contains("9"))
        assertTrue(submitted)
    }

    @Test fun confirmIsOffWithoutACode() {
        show(PairingUiState())

        composeRule.onNodeWithText("Vincular").assertIsNotEnabled()
    }

    @Test fun whileSendingTheActionIsOffAndSaysSo() {
        show(PairingUiState(code = "ABCD2345", status = PairingStatus.Sending))

        composeRule.onNodeWithText("Vinculando…").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test fun eachFailureExplainsWhatToDo() {
        val current = androidx.compose.runtime.mutableStateOf(PairingUiState(code = "ABCD2345"))
        composeRule.setContent { DengoTheme { PairingScreen(current.value, {}, {}) } }
        val expected = mapOf(
            PairingStatus.InvalidCode to "Código inválido ou expirado. Peça outro código.",
            PairingStatus.Locked to "Muitas tentativas. Espere 15 minutos e tente de novo.",
            PairingStatus.NoConnection to "Sem conexão. Confira a internet e tente de novo.",
            PairingStatus.Unavailable to "Não foi possível confirmar. Tente de novo; se não funcionar, peça outro código.",
            PairingStatus.StorageFailed to "Não foi possível guardar o vínculo neste aparelho. Peça outro código.",
        )

        expected.forEach { (status, message) ->
            current.value = PairingUiState(code = "ABCD2345", status = status)
            composeRule.onNodeWithText(message).assertIsDisplayed()
            composeRule.onNodeWithText("Vincular").assertIsEnabled()
        }
    }

    @Test fun theActionHasAnAdequateTouchTarget() {
        show(PairingUiState(code = "ABCD2345"))

        composeRule.onNode(androidx.compose.ui.test.hasClickAction() and androidx.compose.ui.test.hasText("Vincular"))
            .assertHeightIsAtLeast(48.dp)
    }

    @Test fun theScreenShowsNoDataOfTheCouple() {
        show(PairingUiState())

        assertEquals(0, composeRule.onAllNodes(androidx.compose.ui.test.hasText("Mural")).fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodes(androidx.compose.ui.test.hasText("Histórico")).fetchSemanticsNodes().size)
    }
}
