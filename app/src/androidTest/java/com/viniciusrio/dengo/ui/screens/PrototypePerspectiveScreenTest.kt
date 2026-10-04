package com.viniciusrio.dengo.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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

    @Test fun eachPersonKeepsTheExistingSelectionCallback() {
        var selected: PrototypePerspective? = null
        composeRule.setContent {
            DengoTheme {
                PrototypePerspectiveScreen(
                    perspective = PrototypePerspective.VINICIUS,
                    onPerspectiveSelected = { selected = it },
                    contentPadding = PaddingValues(),
                )
            }
        }

        composeRule.onNodeWithText("Vinícius").assertIsSelected()
        composeRule.onNodeWithText("Lidianne").performClick()
        assertEquals(PrototypePerspective.LIDIANNE, selected)
        composeRule.onNodeWithText("Vinícius").performClick()
        assertEquals(PrototypePerspective.VINICIUS, selected)
    }
}
