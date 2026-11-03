package com.viniciusrio.dengo.backend

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode

class MoodAndPersonalSpaceTest : BackendTest() {
    private lateinit var lidianne: String
    private lateinit var vinicius: String

    @BeforeEach
    fun pairBoth() {
        lidianne = pair(Person.LIDIANNE)
        vinicius = pair(Person.VINICIUS)
    }

    private fun setMood(option: String, token: String = lidianne) =
        api.send("PUT", "/v1/mood", token = token, key = newKey(), body = mapOf("option" to option))

    private fun activate(token: String = lidianne) = api.send("POST", "/v1/personal-space", token = token, key = newKey())
    private fun end(token: String = lidianne) = api.send("DELETE", "/v1/personal-space", token = token, key = newKey())

    private fun state(): JsonNode = api.send("GET", "/v1/sync", token = vinicius).body
    private fun events() = (state().get("history") as Iterable<JsonNode>).toList()
    private fun ApiResponse.changed() = body.get("changed").asBoolean()

    // Mood

    @Test
    fun `setting the mood stores it and records one event`() {
        clock.set(Instant.parse("2026-11-03T15:00:00Z"))

        val response = setMood("LOVING")

        assertThat(response.status).isEqualTo(200)
        assertThat(response.changed()).isTrue()
        val mood = state().get("mood")
        assertThat(mood.get("partnerId").asString()).isEqualTo("LIDIANNE")
        assertThat(mood.get("option").asString()).isEqualTo("LOVING")
        assertThat(mood.get("date").asString()).isEqualTo("2026-11-03")
        val event = events().single()
        assertThat(event.get("type").asString()).isEqualTo("MOOD_CHANGED")
        assertThat(event.get("option").asString()).isEqualTo("LOVING")
        assertThat(event.get("actorId").asString()).isEqualTo("LIDIANNE")
    }

    @Test
    fun `repeating the mood on the same day changes nothing and creates no event`() {
        setMood("HAPPY")
        val before = state()

        val repeated = setMood("HAPPY")

        assertThat(repeated.status).isEqualTo(200)
        assertThat(repeated.changed()).isFalse()
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `another mood on the same day is a new event`() {
        setMood("HAPPY")

        assertThat(setMood("TIRED").changed()).isTrue()

        assertThat(state().get("mood").get("option").asString()).isEqualTo("TIRED")
        assertThat(events()).hasSize(2)
    }

    @Test
    fun `the mood date follows the configured zone, not the UTC day`() {
        // 02:59 UTC do dia 4 ainda é 23:59 do dia 3 em São Paulo.
        clock.set(Instant.parse("2026-11-04T02:59:00Z"))
        setMood("HAPPY")

        assertThat(state().get("mood").get("date").asString()).isEqualTo("2026-11-03")
    }

    @Test
    fun `the same mood after midnight in the configured zone is a new day and a new event`() {
        clock.set(Instant.parse("2026-11-04T02:59:00Z"))
        setMood("HAPPY")
        clock.set(Instant.parse("2026-11-04T03:00:00Z"))

        assertThat(setMood("HAPPY").changed()).isTrue()

        assertThat(state().get("mood").get("date").asString()).isEqualTo("2026-11-04")
        assertThat(events()).hasSize(2)
    }

    @Test
    fun `the same mood across the UTC midnight is still the same day in the configured zone`() {
        clock.set(Instant.parse("2026-11-03T23:59:00Z"))
        setMood("HAPPY")
        clock.set(Instant.parse("2026-11-04T00:01:00Z"))

        assertThat(setMood("HAPPY").changed()).isFalse()
    }

    @Test
    fun `Vinicius cannot set the mood`() {
        val before = state()

        val response = setMood("HAPPY", token = vinicius)

        assertThat(response.status).isEqualTo(403)
        assertThat(response.code).isEqualTo("NOT_ALLOWED_FOR_PERSON")
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `an unknown mood is refused`() {
        assertThat(setMood("NOT_A_MOOD").status).isEqualTo(400)
        assertThat(state().get("mood").isNull).isTrue()
    }

    @Test
    fun `every mood of the app is accepted`() {
        val options = listOf("VERY_HAPPY", "HAPPY", "OKAY", "SAD", "UPSET", "TIRED", "LOVING", "NEEDY")

        assertThat(options.map { setMood(it).status }).containsOnly(200)
    }

    // Espaço pessoal

    @Test
    fun `activating personal space stores it and records one event`() {
        clock.set(Instant.parse("2026-11-03T15:00:00Z"))

        assertThat(activate().changed()).isTrue()

        val space = state().get("personalSpace")
        assertThat(space.get("status").asString()).isEqualTo("ACTIVE")
        assertThat(space.get("activatedAt").asString()).isEqualTo("2026-11-03T15:00:00Z")
        assertThat(events().single().get("type").asString()).isEqualTo("PERSONAL_SPACE_ACTIVATED")
    }

    @Test
    fun `activating it again changes nothing and creates no event`() {
        activate()
        val before = state()

        val repeated = activate()

        assertThat(repeated.status).isEqualTo(200)
        assertThat(repeated.changed()).isFalse()
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `ending personal space clears it and records one event`() {
        activate()

        assertThat(end().changed()).isTrue()

        val space = state().get("personalSpace")
        assertThat(space.get("status").asString()).isEqualTo("INACTIVE")
        assertThat(space.get("activatedAt").isNull).isTrue()
        assertThat(events().map { it.get("type").asString() })
            .containsExactly("PERSONAL_SPACE_ACTIVATED", "PERSONAL_SPACE_ENDED")
    }

    @Test
    fun `ending it when inactive changes nothing, from the start or after ending`() {
        val initial = state()
        assertThat(end().changed()).isFalse()
        assertThat(state()).isEqualTo(initial)

        activate()
        end()
        val before = state()
        assertThat(end().changed()).isFalse()
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `Vinicius cannot change personal space`() {
        val before = state()

        assertThat(activate(token = vinicius).status).isEqualTo(403)
        assertThat(end(token = vinicius).status).isEqualTo(403)
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `a pending request can be answered again after personal space ends`() {
        val id = api.send("POST", "/v1/requests", token = lidianne, key = newKey(), body = mapOf("type" to "DENGO"))
            .body.get("request").get("id").asLong()
        activate()
        end()

        assertThat(api.send("POST", "/v1/requests/$id/accept", token = vinicius, key = newKey()).status).isEqualTo(200)
    }

    @Test
    fun `end and activate are distinct commands for the same key`() {
        activate()
        val key = newKey()
        api.send("DELETE", "/v1/personal-space", token = lidianne, key = key)

        val reused = api.send("POST", "/v1/personal-space", token = lidianne, key = key)

        assertThat(reused.code).isEqualTo("IDEMPOTENCY_KEY_REUSED")
    }

    @Test
    fun `sync delivers the latest mood and space, and nothing more once caught up`() {
        setMood("HAPPY")
        activate()
        val revision = state().get("revision").asLong()

        val page = api.send("GET", "/v1/sync?after=$revision", token = vinicius).body

        assertThat(page.get("mood").isNull).isTrue()
        assertThat(page.get("personalSpace").isNull).isTrue()
        assertThat(page.get("history")).isEmpty()
    }
}
