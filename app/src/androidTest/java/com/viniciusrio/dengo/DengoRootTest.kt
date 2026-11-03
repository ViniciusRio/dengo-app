package com.viniciusrio.dengo

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.viniciusrio.dengo.data.session.DeviceSession
import com.viniciusrio.dengo.data.session.HttpPairingClient
import com.viniciusrio.dengo.data.session.SessionStore
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.pairing.PairingViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DengoRootTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(stored: DeviceSession?) {
        val store = object : SessionStore {
            override fun load() = stored
            override fun save(session: DeviceSession) = Unit
        }
        val pairing = PairingViewModel(HttpPairingClient("http://127.0.0.1:9"), store)
        composeRule.setContent { DengoRoot(pairing) { person -> Text("app de ${person.name}") } }
    }

    @Test fun anUnpairedDeviceSeesOnlyThePairingScreen() {
        show(null)

        composeRule.onNodeWithText("Vincular").assertIsDisplayed()
        composeRule.onAllNodesWithText("app de", substring = true).assertCountEquals(0)
    }

    @Test fun aPairedDeviceOpensTheAppOfThePersonOfTheSession() {
        show(DeviceSession(PartnerId.VINICIUS, "token-sintetico"))

        composeRule.onNodeWithText("app de VINICIUS").assertIsDisplayed()
        composeRule.onAllNodesWithText("Vincular").assertCountEquals(0)
    }
}
