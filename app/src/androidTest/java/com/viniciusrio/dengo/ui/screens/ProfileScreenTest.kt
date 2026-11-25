package com.viniciusrio.dengo.ui.screens

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.theme.DengoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(person: PartnerId = PartnerId.LIDIANNE, modifier: Modifier = Modifier) {
        composeRule.setContent {
            DengoTheme {
                Box(modifier) { ProfileScreen(person = person, contentPadding = PaddingValues()) }
            }
        }
    }

    @Test fun profileShowsTheCoupleAndThePersonOfTheSession() {
        show()

        composeRule.onNodeWithText("Perfil").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Lidianne e Vinícius").assertIsDisplayed()
        composeRule.onNodeWithText("Usando o Dengo como").assertIsDisplayed()
        composeRule.onNodeWithText("Lidianne").assertIsDisplayed().assertTextContains("Em uso")
        composeRule.onNodeWithTag("profile-lettering-image").assertIsDisplayed()
    }

    @Test fun theCardInformsWithoutOfferingAChange() {
        show(PartnerId.VINICIUS)

        composeRule.onNodeWithText("Vinícius").assertHasNoClickAction()
        composeRule.onAllNodes(isSelectable()).assertCountEquals(0)
        composeRule.onAllNodesWithText("Lidianne").assertCountEquals(0)
    }

    @Test fun explanationNamesThePersonAndRemainsReachableOnAShortScreen() {
        show(PartnerId.VINICIUS, Modifier.height(400.dp))

        composeRule.onNodeWithText("Este aparelho está vinculado a Vinícius.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test fun letteringFallsBackToTextWithLargeFont() {
        composeRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply { fontScale = 1.6f }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                DengoTheme { ProfileScreen(person = PartnerId.LIDIANNE, contentPadding = PaddingValues()) }
            }
        }

        composeRule.onNodeWithTag("profile-lettering-text").assertExists()
        composeRule.onNodeWithText("Lidianne e Vinícius").assertExists()
    }

    @Test fun letteringFallsBackToTextOnNarrowScreens() {
        show(modifier = Modifier.width(320.dp))

        composeRule.onNodeWithTag("profile-lettering-text").assertExists()
        composeRule.onNodeWithText("Lidianne e Vinícius").assertExists()
    }
}
