package com.viniciusrio.dengo.backend

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode

class SyncTest : BackendTest() {
    private fun other(token: String, text: String) =
        api.send("POST", "/v1/requests", token = token, key = newKey(), body = mapOf("type" to "OTHER", "message" to text))

    private fun sync(token: String, after: Long = 0, limit: Int? = null): ApiResponse =
        api.send("GET", "/v1/sync?after=$after" + (limit?.let { "&limit=$it" } ?: ""), token = token)

    private fun JsonNode.ids(field: String): List<Long> = (get(field) as Iterable<JsonNode>).map { it.get("id").asLong() }

    @Test
    fun `server starts empty`() {
        val page = sync(pair(Person.LIDIANNE)).body

        assertThat(page.get("revision").asLong()).isZero()
        assertThat(page.get("hasMore").asBoolean()).isFalse()
        assertThat(page.get("requests")).isEmpty()
        assertThat(page.get("history")).isEmpty()
        assertThat(page.get("mood").isNull).isTrue()
        assertThat(page.get("personalSpace").isNull).isTrue()
    }

    @Test
    fun `both devices read the same request, id, time and history event`() {
        val lidianne = pair(Person.LIDIANNE)
        val vinicius = pair(Person.VINICIUS)

        // Nanossegundos no relógio: a resposta e a leitura precisam concordar na precisão gravada.
        clock.set(java.time.Instant.parse("2026-11-03T15:00:00.123456789Z"))
        val created = api.send("POST", "/v1/requests", token = lidianne, key = newKey(), body = mapOf("type" to "DENGO"))
        assertThat(created.status).isEqualTo(200)
        assertThat(created.body.get("changed").asBoolean()).isTrue()

        val seenByLidianne = sync(lidianne).body
        val seenByVinicius = sync(vinicius).body
        assertThat(seenByVinicius).isEqualTo(seenByLidianne)

        val request = seenByVinicius.get("requests").single()
        assertThat(request.get("id")).isEqualTo(created.body.get("request").get("id"))
        assertThat(request.get("createdAt").asString()).isEqualTo("2026-11-03T15:00:00.123456Z")
        assertThat(created.body.get("request").get("createdAt").asString()).isEqualTo("2026-11-03T15:00:00.123456Z")
        assertThat(request.get("type").asString()).isEqualTo("DENGO")
        assertThat(request.get("requesterId").asString()).isEqualTo("LIDIANNE")
        assertThat(request.get("recipientId").asString()).isEqualTo("VINICIUS")
        assertThat(request.get("status").asString()).isEqualTo("PENDING")

        val event = seenByVinicius.get("history").single()
        assertThat(event.get("type").asString()).isEqualTo("REQUEST_CREATED")
        assertThat(event.get("requestId")).isEqualTo(request.get("id"))
        assertThat(event.get("actorId").asString()).isEqualTo("LIDIANNE")
        assertThat(event.get("occurredAt").asString()).isEqualTo("2026-11-03T15:00:00.123456Z")
    }

    @Test
    fun `reading after a revision returns only what changed since then`() {
        val lidianne = pair(Person.LIDIANNE)
        val first = other(lidianne, "um").body.get("revision").asLong()
        val secondId = other(lidianne, "dois").body.get("request").get("id").asLong()

        val page = sync(lidianne, after = first).body

        assertThat(page.ids("requests")).containsExactly(secondId)
        assertThat(page.get("revision").asLong()).isEqualTo(first + 1)
        assertThat(sync(lidianne, after = first + 1).body.get("requests")).isEmpty()
    }

    @Test
    fun `pages are cut only between revisions, so limit is approximate`() {
        val lidianne = pair(Person.LIDIANNE)
        repeat(3) { other(lidianne, "pedido $it") }

        // Cada revisão tem duas linhas: o pedido e o acontecimento.
        val first = sync(lidianne, limit = 1).body
        assertThat(first.get("revision").asLong()).isEqualTo(1)
        assertThat(first.get("hasMore").asBoolean()).isTrue()
        assertThat(first.get("requests")).hasSize(1)
        assertThat(first.get("history")).hasSize(1)

        val second = sync(lidianne, after = 1, limit = 3).body
        assertThat(second.get("revision").asLong()).isEqualTo(3)
        assertThat(second.get("hasMore").asBoolean()).isFalse()
        assertThat(second.get("requests")).hasSize(2)
    }

    @Test
    fun `walking the pages delivers every row exactly once`() {
        val lidianne = pair(Person.LIDIANNE)
        val created = (1..7).map { other(lidianne, "pedido $it").body.get("request").get("id").asLong() }

        val seen = mutableListOf<Long>()
        var after = 0L
        do {
            val page = sync(lidianne, after = after, limit = 2).body
            seen += page.ids("requests")
            after = page.get("revision").asLong()
        } while (page.get("hasMore").asBoolean())

        assertThat(seen).containsExactlyElementsOf(created)
    }

    @Test
    fun `out of range paging parameters are refused`() {
        val lidianne = pair(Person.LIDIANNE)

        assertThat(sync(lidianne, limit = 0).status).isEqualTo(400)
        assertThat(sync(lidianne, limit = 501).status).isEqualTo(400)
        assertThat(sync(lidianne, after = -1).status).isEqualTo(400)
    }

    @Test
    fun `reader following concurrent writers never skips a confirmed revision`() {
        val lidianne = pair(Person.LIDIANNE)
        val finished = AtomicBoolean(false)
        val seenEvents = java.util.Collections.synchronizedList(mutableListOf<Long>())

        val reader = CompletableFuture.runAsync {
            var after = 0L
            while (true) {
                val wasFinished = finished.get()
                val page = sync(lidianne, after = after, limit = 5).body
                seenEvents += page.ids("history")
                after = page.get("revision").asLong()
                if (wasFinished && !page.get("hasMore").asBoolean()) break
            }
        }
        val writers = (1..120).map {
            api.sendAsync("POST", "/v1/requests", token = lidianne, key = newKey(), body = mapOf("type" to "OTHER", "message" to "w$it"))
        }
        val responses = writers.map { it.join() }
        finished.set(true)
        reader.join()

        assertThat(responses).allSatisfy { assertThat(it.status).isEqualTo(200) }
        assertThat(responses.map { it.body.get("revision").asLong() }.toSet()).hasSize(120)
        assertThat(seenEvents).hasSize(120).doesNotHaveDuplicates()
    }

    @Test
    fun `a later write cannot pass an open one and make the reader skip it`() {
        val lidianne = pair(Person.LIDIANNE)
        val vinicius = pair(Person.VINICIUS)
        val requestId = other(lidianne, "aberto").body.get("request").get("id").asLong()
        val cursor = sync(lidianne).body.get("revision").asLong()

        val (open, later, duringWrites) = holdingRequestRow(requestId) {
            // A resposta para no meio da transação, ao tocar na linha do pedido.
            val open = api.sendAsync("POST", "/v1/requests/$requestId/accept", token = vinicius, key = newKey())
            awaitBlockedWriters(1)
            val later = api.sendAsync("POST", "/v1/requests", token = lidianne, key = newKey(), body = mapOf("type" to "DENGO"))
            // Com escrita serializada, a segunda fica presa; sem serialização, confirmaria à frente da primeira.
            runCatching { awaitBlockedWriters(2) }
            Triple(open, later, sync(lidianne, after = cursor).body)
        }
        assertThat(open.join().status).isEqualTo(200)
        assertThat(later.join().status).isEqualTo(200)

        val afterWrites = sync(lidianne, after = duringWrites.get("revision").asLong()).body
        val delivered = (duringWrites.get("history") as Iterable<JsonNode>) + (afterWrites.get("history") as Iterable<JsonNode>)
        assertThat(delivered.map { it.get("type").asString() }).containsExactlyInAnyOrder("REQUEST_ACCEPTED", "REQUEST_CREATED")
    }

    @Test
    fun `a long history is walked in bounded pages, with whole revisions and nothing lost`() {
        val lidianne = pair(Person.LIDIANNE)
        // 3.000 revisões sintéticas, cada uma com um acontecimento.
        jdbc.sql("""
            INSERT INTO history_event (type, actor_id, occurred_at, revision)
            SELECT 'PERSONAL_SPACE_ENDED', 'LIDIANNE', now(), g FROM generate_series(1, 3000) g
        """).update()
        jdbc.sql("UPDATE sync_counter SET value = 3000 WHERE id = 1").update()

        var after = 0L
        var seen = 0
        var pages = 0
        do {
            val page = sync(lidianne, after = after, limit = 100).body
            assertThat(page.get("history").size()).isEqualTo(100)
            seen += page.get("history").size()
            after = page.get("revision").asLong()
            pages++
        } while (page.get("hasMore").asBoolean())

        assertThat(seen).isEqualTo(3000)
        assertThat(pages).isEqualTo(30)
        assertThat(after).isEqualTo(3000)
    }

    @Test
    fun `reaching the limit exactly on the last revision reports the current revision with nothing more`() {
        val lidianne = pair(Person.LIDIANNE)
        other(lidianne, "um")

        val page = sync(lidianne, limit = 2).body

        assertThat(page.get("revision").asLong()).isEqualTo(1)
        assertThat(page.get("hasMore").asBoolean()).isFalse()
    }
}
