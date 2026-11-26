package com.viniciusrio.dengo.data.sync

import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.data.sync.FakeCoupleServer.Companion.event
import com.viniciusrio.dengo.data.sync.FakeCoupleServer.Companion.page
import com.viniciusrio.dengo.data.sync.FakeCoupleServer.Companion.request
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteCoupleRepositoryTest {
    private val server = FakeCoupleServer()
    private val store = InMemorySyncStore()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var keys = 0

    private var revocations = 0

    private fun repository(baseUrl: String = server.baseUrl, timeoutMillis: Int = 2_000) = RemoteCoupleRepository(
        api = HttpCoupleApi(baseUrl, token = "token-sintetico", timeoutMillis = timeoutMillis),
        store = store,
        scope = scope,
        onRevoked = { revocations++ },
        newKey = { "key-${++keys}" },
    )

    private fun ready() = repository().also { it.awaitState { true } }

    private fun unreachableUrl(): String {
        val port = java.net.ServerSocket(0).use { it.localPort }
        return "http://127.0.0.1:$port"
    }

    private fun pendingKeys() = runBlocking { store.read().pending.map { it.key } }

    @After fun stop() {
        scope.cancel()
        server.close()
    }

    private fun RemoteCoupleRepository.awaitState(predicate: (CoupleState) -> Boolean): CoupleState = runBlocking {
        withTimeout(5_000) {
            (state.first { it is RepositoryReadState.Ready && predicate(it.value) } as RepositoryReadState.Ready).value
        }
    }

    @Test fun `a confirmed request is sent with a key and the token and reaches the copy`() = runBlocking {
        val repository = repository()
        repository.awaitState { true }
        server.respond("POST /v1/requests", 200, """{"changed":true,"revision":1,"request":${request(5)}}""")
        server.syncPage = { after ->
            if (after < 1) page(1, requests = request(5), history = event(9, "REQUEST_CREATED", "LIDIANNE", requestId = 5)) else page(1)
        }

        val result = repository.createRequest(CareRequestType.DENGO)

        assertTrue(result is WriteResult.Confirmed && result.changed)
        assertEquals(5L, (result as WriteResult.Confirmed).value.id)
        val sent = server.writes().single()
        assertEquals("/v1/requests", sent.path)
        assertEquals("key-1", sent.key)
        assertEquals("Bearer token-sintetico", sent.auth)
        assertEquals("DENGO", JSONObject(sent.body).getString("type"))
        val state = repository.awaitState { it.requests.isNotEmpty() }
        assertEquals(CareRequestStatus.PENDING, state.requests.single().status)
        assertEquals(1, state.history.size)
        assertTrue(store.read().pending.isEmpty())
    }

    @Test fun `a rule refusal reverts with the server code and keeps nothing pending`() = runBlocking {
        val repository = ready()
        server.respond("POST /v1/requests/5/accept", 409, """{"code":"PERSONAL_SPACE_ACTIVE"}""")

        assertEquals(WriteResult.Reverted("PERSONAL_SPACE_ACTIVE"), repository.acceptRequest(5))
        assertTrue(pendingKeys().isEmpty())
        assertEquals(1, server.writes().size)
    }

    @Test fun `failing to connect reverts as no connection and keeps nothing pending`() = runBlocking {
        val repository = repository(baseUrl = unreachableUrl())

        assertEquals(WriteResult.Reverted(RevertReason.NO_CONNECTION), repository.setMood(com.viniciusrio.dengo.model.MoodOption.TIRED))
        assertTrue(pendingKeys().isEmpty())
    }

    @Test fun `a connection lost after sending is uncertain, stays pending and is not resent`() = runBlocking {
        val repository = ready()
        server.dropAfterRequest("POST /v1/personal-space")

        val result = repository.activatePersonalSpace()
        assertTrue("result=$result writes=${server.writes()}", result is WriteResult.Uncertain)
        assertEquals(listOf("key-1"), pendingKeys())
        assertEquals(listOf("key-1"), withTimeout(5_000) { repository.uncertainOperations.first { it.isNotEmpty() } }.map { it.key })
        // Sincroniza para conferir, sem reenviar o comando.
        withTimeout(5_000) { while (server.requests.count { it.method == "GET" } < 2) kotlinx.coroutines.delay(20) }
        assertEquals(1, server.writes().size)
    }

    @Test fun `a server error is uncertain`() = runBlocking {
        val repository = ready()
        server.respond("DELETE /v1/personal-space", 503, "")

        assertTrue(repository.endPersonalSpace() is WriteResult.Uncertain)
        assertEquals(listOf("key-1"), pendingKeys())
    }

    @Test fun `a timeout after sending is uncertain`() = runBlocking {
        val repository = repository(timeoutMillis = 300).also { it.awaitState { true } }
        server.respond("POST /v1/requests/5/decline", 200, """{"changed":true,"revision":2}""")
        server.delayNext("POST /v1/requests/5/decline", 1_000)

        assertTrue(repository.declineRequest(5) is WriteResult.Uncertain)
        assertEquals(listOf("key-1"), pendingKeys())
    }

    @Test fun `a revoked session reverts, clears the copy and the pending operations, and reports the revocation`() = runBlocking {
        server.syncPage = { page(1, requests = request(5)) }
        val repository = ready().also { it.awaitState { state -> state.requests.isNotEmpty() } }
        runBlocking { store.savePending(PendingOperation("old", PendingCommand.EndPersonalSpace)) }
        server.respond("POST /v1/requests/5/acknowledge", 401, """{"code":"SESSION_REVOKED"}""")

        assertEquals(WriteResult.Reverted(RevertReason.SESSION_REVOKED), repository.acknowledgeRequest(5))

        val copy = store.read()
        assertTrue(copy.state.requests.isEmpty())
        assertTrue(copy.pending.isEmpty())
        assertEquals(0, copy.revision)
        assertEquals(1, revocations)
    }

    @Test fun `mural notes are refused locally until stage 6 without contacting the server`() = runBlocking {
        val repository = ready()

        assertEquals(WriteResult.Reverted(RevertReason.MURAL_NOT_SYNCED), repository.createMuralNote("Oi"))
        assertTrue(server.writes().isEmpty())
    }

    private fun uncertainActivation(repository: RemoteCoupleRepository) = runBlocking {
        server.dropAfterRequest("POST /v1/personal-space")
        assertTrue(repository.activatePersonalSpace() is WriteResult.Uncertain)
    }

    @Test fun `retrying resends the same key and content, even after reopening, and a confirmation clears it`() = runBlocking {
        uncertainActivation(ready())
        val reopened = ready()
        assertEquals(listOf("key-1"), withTimeout(5_000) { reopened.uncertainOperations.first { it.isNotEmpty() } }.map { it.key })
        server.respond("POST /v1/personal-space", 200, """{"changed":true,"revision":4}""")

        assertTrue(reopened.retryPending("key-1") is WriteResult.Confirmed)

        val sent = server.writes()
        assertEquals(listOf("key-1", "key-1"), sent.map { it.key })
        assertEquals(sent[0].body, sent[1].body)
        assertTrue(pendingKeys().isEmpty())
        assertTrue(withTimeout(5_000) { reopened.uncertainOperations.first { it.isEmpty() } }.isEmpty())
    }

    @Test fun `the same command as a pending one reuses its key`() = runBlocking {
        val repository = ready()
        uncertainActivation(repository)
        server.respond("POST /v1/personal-space", 200, """{"changed":false,"revision":4}""")

        assertTrue(repository.activatePersonalSpace() is WriteResult.Confirmed)

        assertEquals(listOf("key-1", "key-1"), server.writes().map { it.key })
    }

    @Test fun `a cancelled discard removes the attempt`() = runBlocking {
        val repository = ready()
        uncertainActivation(repository)
        server.respond("DELETE /v1/operations/key-1", 200, """{"outcome":"CANCELLED","revision":null}""")

        assertEquals(DiscardResult.DISCARDED, repository.discardPending("key-1"))
        assertTrue(pendingKeys().isEmpty())
    }

    @Test fun `discarding without connection keeps the attempt pending`() = runBlocking {
        val repository = repository(baseUrl = unreachableUrl())
        runBlocking { store.savePending(PendingOperation("key-1", PendingCommand.ActivatePersonalSpace)) }

        assertEquals(DiscardResult.STILL_PENDING, repository.discardPending("key-1"))
        assertEquals(listOf("key-1"), pendingKeys())
    }

    private val released = java.util.concurrent.atomic.AtomicBoolean(false)

    /** A cópia fica na revisão 3 até o teste liberar a página da revisão 4. */
    private fun holdCopyBelowRevisionFour() {
        server.syncPage = { after ->
            if (released.get()) page(4, personalSpace = """{"status":"ACTIVE","activatedAt":"2026-11-20T12:00:00Z","revision":4}""")
            else page(maxOf(after, 3))
        }
    }

    @Test fun `a discard that finds the write confirmed keeps the key until the copy reaches its revision`() = runBlocking {
        holdCopyBelowRevisionFour()
        val repository = ready()
        uncertainActivation(repository)
        server.respond("DELETE /v1/operations/key-1", 200, """{"outcome":"CONFIRMED","revision":4}""")

        assertEquals(DiscardResult.ALREADY_CONFIRMED, repository.discardPending("key-1"))
        assertTrue(repository.sync())
        assertEquals(listOf("key-1"), pendingKeys())
        assertTrue(repository.uncertainOperations.value.isEmpty())

        released.set(true)
        assertTrue(repository.sync())
        assertTrue(pendingKeys().isEmpty())
    }

    @Test fun `before the copy reaches a confirmed discard, the same command reuses the key`() = runBlocking {
        holdCopyBelowRevisionFour()
        val repository = ready()
        uncertainActivation(repository)
        server.respond("DELETE /v1/operations/key-1", 200, """{"outcome":"CONFIRMED","revision":4}""")
        repository.discardPending("key-1")
        server.respond("POST /v1/personal-space", 200, """{"changed":true,"revision":4}""")

        assertTrue(repository.activatePersonalSpace() is WriteResult.Confirmed)

        assertEquals(listOf("key-1", "key-1"), server.writes().filter { it.method == "POST" }.map { it.key })
    }
}
