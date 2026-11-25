package com.viniciusrio.dengo.data.session

import com.viniciusrio.dengo.model.PartnerId
import java.net.ServerSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HttpPairingClientTest {
    private val server = FakePairingServer()
    private fun client(baseUrl: String = server.baseUrl, timeoutMillis: Int = 2_000) =
        HttpPairingClient(baseUrl, timeoutMillis, Dispatchers.Unconfined)

    @After fun stop() = server.close()

    private fun respond(status: Int, body: String) {
        server.status = status
        server.response = body
    }

    @Test fun `sends the code to the pairing endpoint as json`() = runTest {
        client().pair("ABCD2345")

        val received = server.requests.single()
        assertEquals("POST", received.method)
        assertEquals("/v1/pairings", received.path)
        assertEquals("application/json", received.contentType)
        assertEquals("ABCD2345", JSONObject(received.body).getString("code"))
    }

    @Test fun `a code with quotes is still valid json`() = runTest {
        client().pair("A\"B\\C")

        assertEquals("A\"B\\C", JSONObject(server.requests.single().body).getString("code"))
    }

    @Test fun `valid code returns the session of the person it belongs to`() = runTest {
        respond(200, """{"person":"VINICIUS","token":"abc"}""")

        assertEquals(PairingResult.Paired(DeviceSession(PartnerId.VINICIUS, "abc")), client().pair("X"))
    }

    @Test fun `refused code is reported as invalid`() = runTest {
        respond(400, """{"code":"PAIRING_CODE_INVALID"}""")

        assertEquals(PairingResult.InvalidCode, client().pair("X"))
    }

    @Test fun `too many attempts are reported as locked`() = runTest {
        respond(429, """{"code":"PAIRING_LOCKED"}""")

        assertEquals(PairingResult.Locked, client().pair("X"))
    }

    @Test fun `server error is unavailable, not an invalid code`() = runTest {
        respond(500, """{"status":500}""")

        assertEquals(PairingResult.Unavailable, client().pair("X"))
    }

    @Test fun `an unexpected refusal is unavailable, not an invalid code`() = runTest {
        respond(400, """{"code":"MALFORMED_BODY"}""")

        assertEquals(PairingResult.Unavailable, client().pair("X"))
    }

    @Test fun `a success the app cannot read is unavailable`() = runTest {
        respond(200, "not json")
        assertEquals(PairingResult.Unavailable, client().pair("X"))

        respond(200, """{"person":"ALGUEM","token":"abc"}""")
        assertEquals(PairingResult.Unavailable, client().pair("X"))

        respond(200, """{"person":"LIDIANNE","token":""}""")
        assertEquals(PairingResult.Unavailable, client().pair("X"))
    }

    @Test fun `failing to connect is no connection`() = runTest {
        val closedPort = ServerSocket(0).use { it.localPort }

        assertEquals(PairingResult.NoConnection, client("http://127.0.0.1:$closedPort").pair("X"))
    }

    @Test fun `timing out after sending is unavailable`() = runTest {
        server.delayMillis = 1_000

        assertEquals(PairingResult.Unavailable, client(timeoutMillis = 100).pair("X"))
    }

    @Test fun `the session never prints its token`() {
        assertFalse(DeviceSession(PartnerId.LIDIANNE, "segredo").toString().contains("segredo"))
    }
}
