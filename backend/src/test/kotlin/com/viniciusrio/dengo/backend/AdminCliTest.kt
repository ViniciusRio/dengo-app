package com.viniciusrio.dengo.backend

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AdminCliTest : BackendTest() {
    private fun run(vararg args: String): Pair<Int, String> {
        val out = ByteArrayOutputStream()
        val exit = runAdminCli(
            args.toList() + listOf(
                "--spring.datasource.url=${postgres.jdbcUrl}",
                "--spring.datasource.username=${postgres.username}",
                "--spring.datasource.password=${postgres.password}",
            ),
            PrintStream(out),
        )
        return exit to out.toString().trim()
    }

    @Test
    fun `pairing-code prints a code that pairs the named person`() {
        clock.set(Instant.now()) // o comando roda em outro contexto, com o relógio real
        val (exit, output) = run("pairing-code", "VINICIUS")

        assertThat(exit).isZero()
        val response = api.send("POST", "/v1/pairings", body = mapOf("code" to output.lines().first()))
        assertThat(response.body.get("person").asString()).isEqualTo("VINICIUS")
    }

    @Test
    fun `revoke ends the named person's session`() {
        val token = pair(Person.LIDIANNE)

        assertThat(run("revoke", "LIDIANNE").first).isZero()

        assertThat(api.send("GET", "/v1/sync", token = token).code).isEqualTo("SESSION_REVOKED")
    }

    @Test
    fun `invalidate-all ends every session`() {
        val token = pair(Person.LIDIANNE)

        assertThat(run("invalidate-all").first).isZero()

        assertThat(api.send("GET", "/v1/sync", token = token).code).isEqualTo("SESSION_REVOKED")
    }

    @Test
    fun `unknown command or person fails without changing anything`() {
        val token = pair(Person.LIDIANNE)

        assertThat(run("nope").first).isNotZero()
        assertThat(run("revoke", "NOBODY").first).isNotZero()

        assertThat(api.send("GET", "/v1/sync", token = token).status).isEqualTo(200)
    }
}
