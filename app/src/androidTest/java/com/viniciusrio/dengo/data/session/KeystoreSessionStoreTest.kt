package com.viniciusrio.dengo.data.session

import android.security.NetworkSecurityPolicy
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.viniciusrio.dengo.model.PartnerId
import java.io.File
import java.security.KeyStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreSessionStoreTest {
    // Arquivo e chave próprios: o teste nunca toca a sessão real do aparelho.
    private val alias = "dengo-session-test-key"
    private val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "session-test.bin").also { it.delete() }
    private fun store() = KeystoreSessionStore(file, alias)

    @After fun cleanUp() {
        file.delete()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(alias)
    }

    @Test fun withoutASavedSessionThereIsNone() {
        assertNull(store().load())
    }

    @Test fun aSavedSessionSurvivesANewInstance() {
        val session = DeviceSession(PartnerId.VINICIUS, "token-sintetico")

        store().save(session)

        assertEquals(session, store().load())
    }

    @Test fun theFileDoesNotContainTheToken() {
        store().save(DeviceSession(PartnerId.LIDIANNE, "token-sintetico"))

        val bytes = file.readBytes().decodeToString()
        assertFalse(bytes.contains("token-sintetico"))
        assertFalse(bytes.contains("LIDIANNE"))
    }

    @Test fun savingAgainReplacesTheSession() {
        store().save(DeviceSession(PartnerId.LIDIANNE, "um"))
        store().save(DeviceSession(PartnerId.VINICIUS, "dois"))

        assertEquals(DeviceSession(PartnerId.VINICIUS, "dois"), store().load())
    }

    @Test fun anUnreadableFileIsNoSessionAndIsNotDeleted() {
        store().save(DeviceSession(PartnerId.LIDIANNE, "um"))
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertNull(store().load())
        assertTrue(file.exists())
    }

    @Test fun theDebugBuildAllowsHttpOnlyToTheLocalMachine() {
        val policy = NetworkSecurityPolicy.getInstance()

        assertTrue(policy.isCleartextTrafficPermitted("localhost"))
        assertFalse(policy.isCleartextTrafficPermitted("example.com"))
    }
}
