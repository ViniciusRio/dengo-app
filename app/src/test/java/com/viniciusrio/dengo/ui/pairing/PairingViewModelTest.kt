package com.viniciusrio.dengo.ui.pairing

import com.viniciusrio.dengo.data.session.DeviceSession
import com.viniciusrio.dengo.data.session.FakePairingServer
import com.viniciusrio.dengo.data.session.HttpPairingClient
import com.viniciusrio.dengo.data.session.SessionStore
import com.viniciusrio.dengo.model.PartnerId
import java.net.ServerSocket
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class MemoryStore(var stored: DeviceSession? = null, val failOnSave: Boolean = false) : SessionStore {
    override fun load() = stored
    override fun save(session: DeviceSession) {
        if (failOnSave) throw IOException("sem espaço")
        stored = session
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PairingViewModelTest {
    private val server = FakePairingServer()
    private val store = MemoryStore()
    private fun viewModel(store: SessionStore = this.store, baseUrl: String = server.baseUrl) =
        PairingViewModel(HttpPairingClient(baseUrl, 2_000, Dispatchers.Unconfined), store)

    @Before fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun end() {
        Dispatchers.resetMain()
        server.close()
    }

    private fun PairingViewModel.submit(code: String) {
        updateCode(code)
        submit()
    }

    @Test fun `without a stored session the device is unpaired`() {
        assertNull(viewModel().session.value)
    }

    @Test fun `a stored session opens without a new code`() {
        val stored = DeviceSession(PartnerId.VINICIUS, "abc")

        val model = viewModel(MemoryStore(stored))

        assertEquals(stored, model.session.value)
        assertEquals(0, server.requests.size)
    }

    @Test fun `valid code stores the session and identifies the person`() {
        server.response = """{"person":"VINICIUS","token":"abc"}"""
        val model = viewModel()

        model.submit("abcd2345")

        val expected = DeviceSession(PartnerId.VINICIUS, "abc")
        assertEquals(expected, model.session.value)
        assertEquals(expected, store.stored)
    }

    @Test fun `each refusal keeps the device unpaired, keeps the code and explains itself`() {
        val cases = listOf(
            Triple(400, """{"code":"PAIRING_CODE_INVALID"}""", PairingStatus.InvalidCode),
            Triple(429, """{"code":"PAIRING_LOCKED"}""", PairingStatus.Locked),
            Triple(500, "{}", PairingStatus.Unavailable),
        )
        cases.forEach { (status, body, expected) ->
            server.status = status
            server.response = body
            val model = viewModel()

            model.submit("ABCD2345")

            assertEquals(expected, model.state.value.status)
            assertEquals("ABCD2345", model.state.value.code)
            assertNull(model.session.value)
            assertNull(store.stored)
        }
    }

    @Test fun `failing to connect is explained as no connection`() {
        val closedPort = ServerSocket(0).use { it.localPort }
        val model = viewModel(baseUrl = "http://127.0.0.1:$closedPort")

        model.submit("ABCD2345")

        assertEquals(PairingStatus.NoConnection, model.state.value.status)
    }

    @Test fun `a blank code is not sent`() {
        val model = viewModel()

        model.submit("   ")

        assertEquals(0, server.requests.size)
        assertEquals(PairingStatus.Idle, model.state.value.status)
    }

    @Test fun `editing the code clears the previous error`() {
        server.status = 400
        server.response = """{"code":"PAIRING_CODE_INVALID"}"""
        val model = viewModel()
        model.submit("ABCD2345")

        model.updateCode("ABCD234")

        assertEquals(PairingStatus.Idle, model.state.value.status)
    }

    @Test fun `failing to store the session does not open the app`() {
        val model = viewModel(MemoryStore(failOnSave = true))

        model.submit("ABCD2345")

        assertEquals(PairingStatus.StorageFailed, model.state.value.status)
        assertNull(model.session.value)
    }

    @Test fun `a second submit while sending sends nothing more`() {
        Dispatchers.setMain(StandardTestDispatcher())
        val model = viewModel()

        model.submit("ABCD2345")
        model.submit()

        assertEquals(PairingStatus.Sending, model.state.value.status)
    }
}
