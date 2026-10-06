package com.viniciusrio.dengo.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.navigation.PrototypePerspective
import com.viniciusrio.dengo.ui.theme.DengoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrototypePerspectiveScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun profileShowsCoupleBeforePerspectiveChoicesAndMarksCurrentPerson() {
        composeRule.setContent {
            DengoTheme {
                PrototypePerspectiveScreen(
                    perspective = PrototypePerspective.LIDIANNE,
                    onPerspectiveSelected = {},
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("Perfil").assertIsDisplayed()
        composeRule.onNodeWithText("Lidianne e Vinícius").assertIsDisplayed()
        composeRule.onNodeWithText("Usando o Dengo como").assertIsDisplayed()
        composeRule.onNodeWithText("Lidianne").assertIsSelected()
        composeRule.onNodeWithText("Em uso").assertIsDisplayed()
    }

    @Test fun bothChoicesRemainActionableAndOnlyTheActivePersonShowsInUse() {
        var perspective by mutableStateOf(PrototypePerspective.VINICIUS)
        composeRule.setContent {
            DengoTheme {
                PrototypePerspectiveScreen(
                    perspective = perspective,
                    onPerspectiveSelected = { perspective = it },
                    contentPadding = PaddingValues(),
                )
            }
        }

        assertActivePerson(active = "Vinícius", inactive = "Lidianne")
        composeRule.onNodeWithText("Lidianne").performClick()
        assertEquals(PrototypePerspective.LIDIANNE, perspective)
        assertActivePerson(active = "Lidianne", inactive = "Vinícius")
        composeRule.onNodeWithText("Vinícius").performClick()
        assertEquals(PrototypePerspective.VINICIUS, perspective)
        assertActivePerson(active = "Vinícius", inactive = "Lidianne")
    }

    private fun assertActivePerson(active: String, inactive: String) {
        composeRule.onAllNodes(isSelected()).assertCountEquals(1)
        composeRule.onAllNodesWithText("Em uso").assertCountEquals(1)
        composeRule.onNodeWithText(active).assertIsSelected().assertTextContains("Em uso")
        composeRule.onNodeWithText(inactive).assertIsNotSelected()
    }
}
