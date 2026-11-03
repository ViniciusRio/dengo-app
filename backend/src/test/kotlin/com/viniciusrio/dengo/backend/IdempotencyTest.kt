package com.viniciusrio.dengo.backend

import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode

class IdempotencyTest : BackendTest() {
    private lateinit var lidianne: String
    private lateinit var vinicius: String

    @BeforeEach
    fun pairBoth() {
        lidianne = pair(Person.LIDIANNE)
        vinicius = pair(Person.VINICIUS)
    }

    private val note = mapOf("type" to "OTHER", "message" to "um chá")

    private fun create(key: String?, body: Any = note, token: String = lidianne) =
        api.send("POST", "/v1/requests", token = token, key = key, body = body)

    private fun createAsync(key: String, body: Any = note) =
        api.sendAsync("POST", "/v1/requests", token = lidianne, key = key, body = body)

    private fun discard(key: String, token: String = lidianne) = api.send("DELETE", "/v1/operations/$key", token = token)
    private fun discardAsync(key: String) = api.sendAsync("DELETE", "/v1/operations/$key", token = lidianne)

    private fun state(): JsonNode = api.send("GET", "/v1/sync", token = lidianne).body
    private fun requestCount() = state().get("requests").size()
    private fun eventCount() = state().get("history").size()

    @Test
    fun `repeating a key returns the original result and changes nothing`() {
        val key = newKey()
        val original = create(key)
        clock.advance(Duration.ofHours(1))

        val repeated = create(key)

        assertThat(repeated.status).isEqualTo(200)
        assertThat(repeated.body).isEqualTo(original.body)
        assertThat(requestCount()).isEqualTo(1)
        assertThat(eventCount()).isEqualTo(1)
    }

    @Test
    fun `reusing a key with other content is refused and changes nothing`() {
        val key = newKey()
        create(key)
        val before = state()

        val reused = create(key, mapOf("type" to "OTHER", "message" to "outro texto"))

        assertThat(reused.status).isEqualTo(409)
        assertThat(reused.code).isEqualTo("IDEMPOTENCY_KEY_REUSED")
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `reusing a key on another command is refused`() {
        val id = create(newKey()).body.get("request").get("id").asLong()
        val key = newKey()
        api.send("POST", "/v1/requests/$id/accept", token = vinicius, key = key)
        val other = create(newKey()).body.get("request").get("id").asLong()

        val reused = api.send("POST", "/v1/requests/$other/decline", token = vinicius, key = key)

        assertThat(reused.code).isEqualTo("IDEMPOTENCY_KEY_REUSED")
    }

    @Test
    fun `a rule refusal is the operation's result and repeats even when the rule would now pass`() {
        val id = create(newKey()).body.get("request").get("id").asLong()
        val key = newKey()
        val refused = api.send("POST", "/v1/requests/$id/acknowledge", token = lidianne, key = key)
        assertThat(refused.status).isEqualTo(409)
        api.send("POST", "/v1/requests/$id/accept", token = vinicius, key = newKey())

        val repeated = api.send("POST", "/v1/requests/$id/acknowledge", token = lidianne, key = key)

        assertThat(repeated.status).isEqualTo(409)
        assertThat(repeated.code).isEqualTo("INVALID_TRANSITION")
        assertThat(eventCount()).isEqualTo(2)
    }

    @Test
    fun `a result without change repeats the same way, even after the type is free again`() {
        val dengo = mapOf("type" to "DENGO")
        val id = create(newKey(), dengo).body.get("request").get("id").asLong()
        val key = newKey()
        val duplicate = create(key, dengo)
        api.send("POST", "/v1/requests/$id/decline", token = vinicius, key = newKey())

        val repeated = create(key, dengo)

        assertThat(repeated.body).isEqualTo(duplicate.body)
        assertThat(repeated.body.get("changed").asBoolean()).isFalse()
        assertThat(requestCount()).isEqualTo(1)
    }

    @Test
    fun `keys belong to each person`() {
        val key = newKey()
        val id = create(key).body.get("request").get("id").asLong()

        val accepted = api.send("POST", "/v1/requests/$id/accept", token = vinicius, key = key)

        assertThat(accepted.status).isEqualTo(200)
        assertThat(accepted.body.get("changed").asBoolean()).isTrue()
    }

    @Test
    fun `keys have no expiry and survive a new pairing of the same person`() {
        val key = newKey()
        val original = create(key)
        clock.advance(Duration.ofDays(3650))
        lidianne = pair(Person.LIDIANNE)

        assertThat(create(key).body).isEqualTo(original.body)
        assertThat(requestCount()).isEqualTo(1)
    }

    @Test
    fun `a command without a key is refused`() {
        val response = create(null)

        assertThat(response.status).isEqualTo(400)
        assertThat(response.code).isEqualTo("IDEMPOTENCY_KEY_REQUIRED")
        assertThat(requestCount()).isZero()
    }

    @Test
    fun `a key that would not fit the discard path is refused`() {
        val response = create("não/vale")

        assertThat(response.status).isEqualTo(400)
        assertThat(response.code).isEqualTo("IDEMPOTENCY_KEY_INVALID")
        assertThat(requestCount()).isZero()
        assertThat(create("a".repeat(129)).code).isEqualTo("IDEMPOTENCY_KEY_INVALID")
    }

    // Segundo envio com a primeira transação em andamento

    @Test
    fun `second send waits for the open transaction and returns its result`() {
        val key = newKey()
        val (first, second) = holdingWriteLock {
            val first = createAsync(key)
            awaitBlockedWriters(1)
            val second = createAsync(key)
            awaitBlockedWriters(2)
            first to second
        }

        assertThat(first.join().status).isEqualTo(200)
        assertThat(second.join().body).isEqualTo(first.join().body)
        assertThat(requestCount()).isEqualTo(1)
        assertThat(eventCount()).isEqualTo(1)
    }

    @Test
    fun `second send writes once when the open transaction fails`() {
        val key = newKey()
        val (first, second) = holdingWriteLock {
            val first = createAsync(key)
            awaitBlockedWriters(1)
            val second = createAsync(key)
            awaitBlockedWriters(2)
            cancelOldestBlockedWriter()
            first to second
        }

        assertThat(first.join().status).isEqualTo(500)
        assertThat(second.join().status).isEqualTo(200)
        assertThat(requestCount()).isEqualTo(1)
        assertThat(eventCount()).isEqualTo(1)
    }

    @Test
    fun `second send with other content waits and is refused`() {
        val key = newKey()
        val (first, second) = holdingWriteLock {
            val first = createAsync(key)
            awaitBlockedWriters(1)
            val second = createAsync(key, mapOf("type" to "OTHER", "message" to "outro"))
            awaitBlockedWriters(2)
            first to second
        }

        assertThat(first.join().status).isEqualTo(200)
        assertThat(second.join().code).isEqualTo("IDEMPOTENCY_KEY_REUSED")
        assertThat(requestCount()).isEqualTo(1)
    }

    // Descarte

    @Test
    fun `discarding an unused key cancels it and a late send cannot confirm`() {
        val key = newKey()

        val discarded = discard(key)
        val late = create(key)

        assertThat(discarded.status).isEqualTo(200)
        assertThat(discarded.body.get("outcome").asString()).isEqualTo("CANCELLED")
        assertThat(late.status).isEqualTo(409)
        assertThat(late.code).isEqualTo("OPERATION_CANCELLED")
        assertThat(requestCount()).isZero()
        assertThat(eventCount()).isZero()
    }

    @Test
    fun `discarding twice gives the same definitive answer`() {
        val key = newKey()
        discard(key)

        assertThat(discard(key).body.get("outcome").asString()).isEqualTo("CANCELLED")
    }

    @Test
    fun `discarding a confirmed write returns its revision and the original result stays`() {
        val key = newKey()
        val original = create(key)
        create(newKey())

        val discarded = discard(key)

        assertThat(discarded.status).isEqualTo(200)
        assertThat(discarded.body.get("outcome").asString()).isEqualTo("CONFIRMED")
        assertThat(discarded.body.get("revision")).isEqualTo(original.body.get("revision"))
        assertThat(create(key).body).isEqualTo(original.body)
        assertThat(requestCount()).isEqualTo(2)
    }

    @Test
    fun `discarding a result without change returns a revision the copy can already reach`() {
        val dengo = mapOf("type" to "DENGO")
        val created = create(newKey(), dengo)
        val key = newKey()
        create(key, dengo)

        val discarded = discard(key)

        assertThat(discarded.body.get("outcome").asString()).isEqualTo("CONFIRMED")
        assertThat(discarded.body.get("revision")).isEqualTo(created.body.get("revision"))
    }

    @Test
    fun `discarding a refused operation reports that nothing was written`() {
        val id = create(newKey()).body.get("request").get("id").asLong()
        val key = newKey()
        api.send("POST", "/v1/requests/$id/acknowledge", token = lidianne, key = key)

        val discarded = discard(key)

        assertThat(discarded.status).isEqualTo(200)
        assertThat(discarded.body.get("outcome").asString()).isEqualTo("REFUSED")
    }

    @Test
    fun `discard waits for the open transaction and then the same command gets the original result`() {
        val key = newKey()
        val (first, discarded) = holdingWriteLock {
            val first = createAsync(key)
            awaitBlockedWriters(1)
            val discarded = discardAsync(key)
            awaitBlockedWriters(2)
            first to discarded
        }

        val original = first.join()
        assertThat(discarded.join().body.get("outcome").asString()).isEqualTo("CONFIRMED")
        assertThat(discarded.join().body.get("revision")).isEqualTo(original.body.get("revision"))
        assertThat(create(key).body).isEqualTo(original.body)
        assertThat(requestCount()).isEqualTo(1)
        assertThat(eventCount()).isEqualTo(1)
    }

    @Test
    fun `discard cancels the key when the open transaction fails, and a late send cannot confirm`() {
        val key = newKey()
        val (first, discarded) = holdingWriteLock {
            val first = createAsync(key)
            awaitBlockedWriters(1)
            val discarded = discardAsync(key)
            awaitBlockedWriters(2)
            cancelOldestBlockedWriter()
            first to discarded
        }

        assertThat(first.join().status).isEqualTo(500)
        assertThat(discarded.join().body.get("outcome").asString()).isEqualTo("CANCELLED")
        assertThat(create(key).code).isEqualTo("OPERATION_CANCELLED")
        assertThat(requestCount()).isZero()
    }

    @Test
    fun `one person's discard does not touch the other's key`() {
        val key = newKey()
        val original = create(key)

        assertThat(discard(key, token = vinicius).body.get("outcome").asString()).isEqualTo("CANCELLED")

        assertThat(create(key).body).isEqualTo(original.body)
    }

    @Test
    fun `discard without a valid session is refused`() {
        assertThat(api.send("DELETE", "/v1/operations/${newKey()}", token = "nope").code).isEqualTo("SESSION_REVOKED")
    }
}
