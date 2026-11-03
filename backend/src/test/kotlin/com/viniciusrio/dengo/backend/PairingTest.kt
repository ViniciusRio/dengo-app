package com.viniciusrio.dengo.backend

import java.time.Duration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PairingTest : BackendTest() {
    @Test
    fun `valid code creates a session for the person it was issued to`() {
        val code = admin.issuePairingCode(Person.LIDIANNE).code

        val response = api.send("POST", "/v1/pairings", body = mapOf("code" to code))

        assertThat(response.status).isEqualTo(200)
        assertThat(response.body.get("person").asString()).isEqualTo("LIDIANNE")
        assertThat(api.send("GET", "/v1/sync", token = response.body.get("token").asString()).status).isEqualTo(200)
    }

    @Test
    fun `request without credentials is refused with a code that does not wipe the device`() {
        val response = api.send("GET", "/v1/sync")

        assertThat(response.status).isEqualTo(401)
        assertThat(response.code).isEqualTo("AUTH_REQUIRED")
    }

    @Test
    fun `unknown token is refused with the revocation code`() {
        val response = api.send("GET", "/v1/sync", token = "unknown")

        assertThat(response.status).isEqualTo(401)
        assertThat(response.code).isEqualTo("SESSION_REVOKED")
    }

    @Test
    fun `code is valid for just under 15 minutes and refused when it expires`() {
        val first = admin.issuePairingCode(Person.LIDIANNE).code
        clock.advance(Duration.ofMinutes(15).minusSeconds(1))
        assertThat(attempt(first).status).isEqualTo(200)

        val second = admin.issuePairingCode(Person.LIDIANNE).code
        clock.advance(Duration.ofMinutes(15))
        val expired = attempt(second)

        assertThat(expired.status).isEqualTo(400)
        assertThat(expired.code).isEqualTo("PAIRING_CODE_INVALID")
    }

    @Test
    fun `code can only be used once and a refused attempt creates no session`() {
        val code = admin.issuePairingCode(Person.LIDIANNE).code
        val token = attempt(code).body.get("token").asString()

        val reused = attempt(code)

        assertThat(reused.code).isEqualTo("PAIRING_CODE_INVALID")
        assertThat(api.send("GET", "/v1/sync", token = token).status).isEqualTo(200)
        assertThat(sessionCount()).isEqualTo(1)
    }

    @Test
    fun `two simultaneous uses of one code create a single session`() {
        val code = admin.issuePairingCode(Person.VINICIUS).code

        val responses = (1..2).map { api.sendAsync("POST", "/v1/pairings", body = mapOf("code" to code)) }.map { it.join() }

        assertThat(responses.map { it.status }).containsExactlyInAnyOrder(200, 400)
        assertThat(sessionCount()).isEqualTo(1)
    }

    @Test
    fun `issuing a new code invalidates the previous unused one`() {
        val old = admin.issuePairingCode(Person.LIDIANNE).code
        val current = admin.issuePairingCode(Person.LIDIANNE).code

        assertThat(attempt(old).status).isEqualTo(400)
        assertThat(attempt(current).status).isEqualTo(200)
    }

    @Test
    fun `sixth attempt after five invalid ones is refused even with a correct code`() {
        val code = admin.issuePairingCode(Person.LIDIANNE).code
        repeat(5) { assertThat(attempt("WRONG").code).isEqualTo("PAIRING_CODE_INVALID") }

        val locked = attempt(code)

        assertThat(locked.status).isEqualTo(429)
        assertThat(locked.code).isEqualTo("PAIRING_LOCKED")
        assertThat(sessionCount()).isZero()
    }

    @Test
    fun `lock ends after 15 minutes and the attempt count starts over`() {
        val code = admin.issuePairingCode(Person.LIDIANNE).code
        repeat(5) { attempt("WRONG") }

        clock.advance(Duration.ofMinutes(15).minusSeconds(1))
        assertThat(attempt(code).status).isEqualTo(429)
        clock.advance(Duration.ofSeconds(1))
        repeat(4) { assertThat(attempt("WRONG").status).isEqualTo(400) }

        assertThat(attempt(admin.issuePairingCode(Person.LIDIANNE).code).status).isEqualTo(200)
    }

    @Test
    fun `a successful pairing clears previous invalid attempts`() {
        repeat(4) { attempt("WRONG") }
        attempt(admin.issuePairingCode(Person.LIDIANNE).code)

        repeat(4) { assertThat(attempt("WRONG").status).isEqualTo(400) }

        assertThat(attempt(admin.issuePairingCode(Person.VINICIUS).code).status).isEqualTo(200)
    }

    @Test
    fun `an expired or reused code also counts as an invalid attempt`() {
        val code = admin.issuePairingCode(Person.LIDIANNE).code
        attempt(code)
        repeat(5) { assertThat(attempt(code).status).isEqualTo(400) }

        assertThat(attempt(admin.issuePairingCode(Person.VINICIUS).code).status).isEqualTo(429)
    }

    @Test
    fun `the session table keeps only a hash the original token cannot be read from`() {
        val token = pair(Person.LIDIANNE)

        val stored = jdbc.sql("SELECT token_hash FROM device_session").query(String::class.java).single()

        assertThat(stored).isNotEqualTo(token).doesNotContain(token)
        assertThat(api.send("GET", "/v1/sync", token = stored).code).isEqualTo("SESSION_REVOKED")
    }

    @Test
    fun `pairing a new device revokes only the same person's previous session`() {
        val lidianneOld = pair(Person.LIDIANNE)
        val vinicius = pair(Person.VINICIUS)

        val lidianneNew = pair(Person.LIDIANNE)

        assertThat(api.send("GET", "/v1/sync", token = lidianneOld).code).isEqualTo("SESSION_REVOKED")
        assertThat(api.send("GET", "/v1/sync", token = lidianneNew).status).isEqualTo(200)
        assertThat(api.send("GET", "/v1/sync", token = vinicius).status).isEqualTo(200)
    }

    @Test
    fun `administrative revocation refuses that person's session and leaves the other alone`() {
        val lidianne = pair(Person.LIDIANNE)
        val vinicius = pair(Person.VINICIUS)

        admin.revoke(Person.LIDIANNE)

        assertThat(api.send("GET", "/v1/sync", token = lidianne).code).isEqualTo("SESSION_REVOKED")
        assertThat(api.send("GET", "/v1/sync", token = vinicius).status).isEqualTo(200)
    }

    @Test
    fun `invalidating everything refuses every session and every pending code`() {
        val token = pair(Person.LIDIANNE)
        val pending = admin.issuePairingCode(Person.VINICIUS).code

        admin.invalidateAll()

        assertThat(api.send("GET", "/v1/sync", token = token).code).isEqualTo("SESSION_REVOKED")
        assertThat(attempt(pending).code).isEqualTo("PAIRING_CODE_INVALID")
        assertThat(attempt(admin.issuePairingCode(Person.VINICIUS).code).status).isEqualTo(200)
    }

    private fun attempt(code: String) = api.send("POST", "/v1/pairings", body = mapOf("code" to code))

    private fun sessionCount() = jdbc.sql("SELECT count(*) FROM device_session WHERE revoked_at IS NULL").query(Int::class.java).single()
}
