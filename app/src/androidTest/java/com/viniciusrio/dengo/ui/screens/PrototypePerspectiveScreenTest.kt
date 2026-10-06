package com.viniciusrio.dengo.ui.screens

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
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
        composeRule.onNodeWithContentDescription("Lidianne e Vinícius").assertIsDisplayed()
        composeRule.onNodeWithText("Usando o Dengo como").assertIsDisplayed()
        composeRule.onNodeWithText("Lidianne").assertIsSelected()
        composeRule.onNodeWithText("Em uso").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-lettering-image").assertIsDisplayed()
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
        composeRule.onNodeWithText(active).assertHasClickAction()
        composeRule.onNodeWithText(inactive).assertHasClickAction()
        val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        composeRule.onNodeWithText(active).assert(radioRole)
        composeRule.onNodeWithText(inactive).assert(radioRole)
    }

    @Test fun explanationRemainsReachableWhenTheScreenIsShort() {
        composeRule.setContent {
            DengoTheme {
                Box(Modifier.height(400.dp)) {
                    PrototypePerspectiveScreen(
                        perspective = PrototypePerspective.LIDIANNE,
                        onPerspectiveSelected = {},
                        contentPadding = PaddingValues(),
                    )
                }
            }
        }

        composeRule.onNodeWithText("Escolher uma pessoa abre a Home dela neste aparelho.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test fun letteringFallsBackToTextWithLargeFont() {
        composeRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply { fontScale = 1.6f }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                DengoTheme {
                    PrototypePerspectiveScreen(
                        perspective = PrototypePerspective.LIDIANNE,
                        onPerspectiveSelected = {},
                        contentPadding = PaddingValues(),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("profile-lettering-text").assertExists()
        composeRule.onNodeWithText("Lidianne e Vinícius").assertExists()
    }

    @Test fun letteringFallsBackToTextOnNarrowScreens() {
        composeRule.setContent {
            DengoTheme {
                Box(Modifier.width(320.dp)) {
                    PrototypePerspectiveScreen(
                        perspective = PrototypePerspective.LIDIANNE,
                        onPerspectiveSelected = {},
                        contentPadding = PaddingValues(),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("profile-lettering-text").assertExists()
        composeRule.onNodeWithText("Lidianne e Vinícius").assertExists()
    }
}
