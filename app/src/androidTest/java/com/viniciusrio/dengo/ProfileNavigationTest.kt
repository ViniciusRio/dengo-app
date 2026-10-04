package com.viniciusrio.dengo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileNavigationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun changingPerspectiveOpensTheMatchingHomeAndSetsMuralAuthorship() {
        composeRule.onNodeWithText("Perfil").performClick()
        composeRule.onNodeWithText("Vinícius").performClick()
        composeRule.onNodeWithText("Olá, Vinícius").assertIsDisplayed()

        composeRule.onNodeWithText("Mural").performClick()
        composeRule.onNodeWithText("Deixar um recado").performClick()
        composeRule.onNodeWithText("Escrever").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Recado do Vinícius")
        composeRule.onNodeWithText("Deixar recado").performClick()
        composeRule.onNodeWithText("Recado do Vinícius").assertIsDisplayed()
        composeRule.onNodeWithText("Vinícius").assertIsDisplayed()

        composeRule.onNodeWithText("Perfil").performClick()
        composeRule.onNodeWithText("Lidianne").performClick()
        composeRule.onNodeWithText("Olá, Lidianne").assertIsDisplayed()
        composeRule.onNodeWithText("Mural").performClick()
        composeRule.onNodeWithText("Recado do Vinícius").assertIsDisplayed()
    }
}
