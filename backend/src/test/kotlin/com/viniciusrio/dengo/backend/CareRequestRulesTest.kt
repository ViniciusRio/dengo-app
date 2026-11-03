package com.viniciusrio.dengo.backend

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tools.jackson.databind.JsonNode

class CareRequestRulesTest : BackendTest() {
    private lateinit var lidianne: String
    private lateinit var vinicius: String

    @BeforeEach
    fun pairBoth() {
        lidianne = pair(Person.LIDIANNE)
        vinicius = pair(Person.VINICIUS)
    }

    private fun create(type: String, message: String? = null, token: String = lidianne) =
        api.send("POST", "/v1/requests", token = token, key = newKey(), body = mapOf("type" to type, "message" to message))

    private fun act(id: Long, action: String, token: String) =
        api.send("POST", "/v1/requests/$id/$action", token = token, key = newKey())

    private fun createdId(type: String = "DENGO") = create(type).body.get("request").get("id").asLong()
    private fun state(): JsonNode = api.send("GET", "/v1/sync", token = lidianne).body
    private fun JsonNode.items(): List<JsonNode> = (this as Iterable<JsonNode>).toList()
    private fun status(id: Long) = state().get("requests").items().single { it.get("id").asLong() == id }.get("status").asString()
    private fun historyTypes() = state().get("history").items().map { it.get("type").asString() }

    private fun assertRefusedWithoutChange(response: ApiResponse, status: Int, code: String, before: JsonNode) {
        assertThat(response.status).isEqualTo(status)
        assertThat(response.code).isEqualTo(code)
        assertThat(state()).isEqualTo(before)
    }

    // Duplicidade

    @Test
    fun `duplicate predefined request returns the active one without a new event`() {
        val first = create("DENGO").body.get("request")
        val before = state()

        val duplicate = create("DENGO")

        assertThat(duplicate.status).isEqualTo(200)
        assertThat(duplicate.body.get("changed").asBoolean()).isFalse()
        assertThat(duplicate.body.get("request")).isEqualTo(first)
        assertThat(state()).isEqualTo(before)
    }

    @Test
    fun `accepted request still blocks a new one of the same type`() {
        val id = createdId()
        act(id, "accept", vinicius)

        assertThat(create("DENGO").body.get("changed").asBoolean()).isFalse()
    }

    @Test
    fun `different predefined types do not block each other`() {
        createdId("DENGO")

        assertThat(create("MEDICINE").body.get("changed").asBoolean()).isTrue()
    }

    @Test
    fun `declined and acknowledged requests free the type immediately`() {
        act(createdId(), "decline", vinicius)
        val second = createdId()
        act(second, "accept", vinicius)
        act(second, "acknowledge", lidianne)

        assertThat(create("DENGO").body.get("changed").asBoolean()).isTrue()
    }

    @Test
    fun `OTHER is always independent, even with the same text`() {
        assertThat(create("OTHER", "igual").body.get("changed").asBoolean()).isTrue()
        assertThat(create("OTHER", "igual").body.get("changed").asBoolean()).isTrue()
        assertThat(state().get("requests")).hasSize(2)
    }

    @Test
    fun `simultaneous requests of one type create a single request and event`() {
        val responses = (1..8)
            .map { api.sendAsync("POST", "/v1/requests", token = lidianne, key = newKey(), body = mapOf("type" to "MEDICINE")) }
            .map { it.join() }

        assertThat(responses).allSatisfy { assertThat(it.status).isEqualTo(200) }
        assertThat(responses.count { it.body.get("changed").asBoolean() }).isEqualTo(1)
        assertThat(state().get("requests")).hasSize(1)
        assertThat(historyTypes()).containsExactly("REQUEST_CREATED")
    }

    // Texto

    @Test
    fun `OTHER message is trimmed`() {
        val response = create("OTHER", "  chá, por favor \n")

        assertThat(response.body.get("request").get("message").asString()).isEqualTo("chá, por favor")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\n\t"])
    fun `OTHER without text is refused`(message: String) {
        val before = state()
        assertRefusedWithoutChange(create("OTHER", message), 422, "INVALID_REQUEST", before)
    }

    @Test
    fun `OTHER with no message field is refused`() {
        val before = state()
        assertRefusedWithoutChange(create("OTHER"), 422, "INVALID_REQUEST", before)
    }

    @Test
    fun `predefined request with a message is refused`() {
        val before = state()
        assertRefusedWithoutChange(create("DENGO", "texto"), 422, "INVALID_REQUEST", before)
    }

    @Test
    fun `malformed bodies and unknown types are refused without echoing content`() {
        val response = api.send("POST", "/v1/requests", token = lidianne, key = newKey(), body = """{"type":"segredo afetivo"}""")

        assertThat(response.status).isEqualTo(400)
        assertThat(response.code).isEqualTo("MALFORMED_BODY")
        assertThat(response.body.toString()).doesNotContain("segredo")
    }

    // Transições

    @Test
    fun `pending request can be accepted and records one event`() {
        val id = createdId()

        val response = act(id, "accept", vinicius)

        assertThat(response.status).isEqualTo(200)
        assertThat(response.body.get("changed").asBoolean()).isTrue()
        assertThat(status(id)).isEqualTo("ACCEPTED")
        assertThat(historyTypes()).containsExactly("REQUEST_CREATED", "REQUEST_ACCEPTED")
        val event = state().get("history").items().last()
        assertThat(event.get("actorId").asString()).isEqualTo("VINICIUS")
        assertThat(event.get("requestId").asLong()).isEqualTo(id)
    }

    @Test
    fun `pending request can be declined and records one event`() {
        val id = createdId()

        assertThat(act(id, "decline", vinicius).status).isEqualTo(200)

        assertThat(status(id)).isEqualTo("DECLINED")
        assertThat(historyTypes()).containsExactly("REQUEST_CREATED", "REQUEST_DECLINED")
    }

    @Test
    fun `accepted request can be acknowledged by Lidianne`() {
        val id = createdId()
        act(id, "accept", vinicius)

        assertThat(act(id, "acknowledge", lidianne).status).isEqualTo(200)

        assertThat(status(id)).isEqualTo("ACKNOWLEDGED")
        assertThat(historyTypes()).containsExactly("REQUEST_CREATED", "REQUEST_ACCEPTED", "REQUEST_ACKNOWLEDGED")
    }

    @Test
    fun `responding to a request no longer pending is refused without an event`() {
        val id = createdId()
        act(id, "accept", vinicius)
        val before = state()

        assertRefusedWithoutChange(act(id, "accept", vinicius), 409, "INVALID_TRANSITION", before)
        assertRefusedWithoutChange(act(id, "decline", vinicius), 409, "INVALID_TRANSITION", before)
    }

    @Test
    fun `acknowledging a request that is not accepted is refused`() {
        val pending = createdId("DENGO")
        val declined = createdId("MEDICINE")
        act(declined, "decline", vinicius)
        val before = state()

        assertRefusedWithoutChange(act(pending, "acknowledge", lidianne), 409, "INVALID_TRANSITION", before)
        assertRefusedWithoutChange(act(declined, "acknowledge", lidianne), 409, "INVALID_TRANSITION", before)
    }

    @Test
    fun `acting on an unknown request is refused`() {
        val before = state()

        assertRefusedWithoutChange(act(999, "accept", vinicius), 404, "REQUEST_NOT_FOUND", before)
        assertRefusedWithoutChange(act(999, "acknowledge", lidianne), 404, "REQUEST_NOT_FOUND", before)
    }

    // Espaço pessoal

    @Test
    fun `response during active personal space is refused and leaves the request pending`() {
        val id = createdId()
        api.send("POST", "/v1/personal-space", token = lidianne, key = newKey())
        val before = state()

        assertRefusedWithoutChange(act(id, "accept", vinicius), 409, "PERSONAL_SPACE_ACTIVE", before)
        assertRefusedWithoutChange(act(id, "decline", vinicius), 409, "PERSONAL_SPACE_ACTIVE", before)
        assertThat(status(id)).isEqualTo("PENDING")
    }

    @Test
    fun `request can still be created and acknowledged during active personal space`() {
        val accepted = createdId()
        act(accepted, "accept", vinicius)
        api.send("POST", "/v1/personal-space", token = lidianne, key = newKey())

        assertThat(create("MEDICINE").body.get("changed").asBoolean()).isTrue()
        assertThat(act(accepted, "acknowledge", lidianne).status).isEqualTo(200)
    }

    // Permissão por pessoa

    @Test
    fun `Vinicius cannot create requests or acknowledge`() {
        val id = createdId()
        act(id, "accept", vinicius)
        val before = state()

        assertRefusedWithoutChange(create("MEDICINE", token = vinicius), 403, "NOT_ALLOWED_FOR_PERSON", before)
        assertRefusedWithoutChange(act(id, "acknowledge", vinicius), 403, "NOT_ALLOWED_FOR_PERSON", before)
    }

    @Test
    fun `Lidianne cannot accept or decline`() {
        val id = createdId()
        val before = state()

        assertRefusedWithoutChange(act(id, "accept", lidianne), 403, "NOT_ALLOWED_FOR_PERSON", before)
        assertRefusedWithoutChange(act(id, "decline", lidianne), 403, "NOT_ALLOWED_FOR_PERSON", before)
    }

    @Test
    fun `a client cannot name another author in the body`() {
        val response = api.send(
            "POST", "/v1/requests", token = vinicius, key = newKey(),
            body = mapOf("type" to "DENGO", "requesterId" to "LIDIANNE", "actorId" to "LIDIANNE"),
        )

        assertThat(response.status).isEqualTo(403)
    }

    // Atomicidade

    @Test
    fun `a failure while recording the history event leaves no partial write`() {
        val id = createdId()
        val before = state()
        // Falha real do banco no meio da transação, sem gancho no código de produção.
        jdbc.sql("ALTER TABLE history_event ADD CONSTRAINT refuse_events CHECK (type <> 'REQUEST_ACCEPTED') NOT VALID").update()
        try {
            assertThat(act(id, "accept", vinicius).status).isEqualTo(500)
        } finally {
            jdbc.sql("ALTER TABLE history_event DROP CONSTRAINT refuse_events").update()
        }

        assertThat(state()).isEqualTo(before)
        assertThat(status(id)).isEqualTo("PENDING")
    }
}
