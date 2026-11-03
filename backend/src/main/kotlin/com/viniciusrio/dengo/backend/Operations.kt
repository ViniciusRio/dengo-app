package com.viniciusrio.dengo.backend

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.json.JsonMapper

data class CommandResult(val status: Int, val body: Any)
data class StoredResponse(val status: Int, val json: String)

enum class DiscardOutcome { CANCELLED, CONFIRMED, REFUSED }
data class DiscardResult(val outcome: DiscardOutcome, val revision: Long?)

private val KEY_FORMAT = Regex("[A-Za-z0-9_-]{1,128}")

internal fun isValidOperationKey(key: String) = KEY_FORMAT.matches(key)

/** A revisão da transação em curso. Quem a usa já detém o bloqueio do contador. */
class WriteContext internal constructor(private val jdbc: JdbcClient, current: Long) {
    var revision: Long = current
        private set

    fun nextRevision(): Long {
        revision = jdbc.sql("UPDATE sync_counter SET value = value + 1 WHERE id = 1 RETURNING value")
            .query(Long::class.java).single()
        return revision
    }
}

private data class OperationRow(
    val state: String,
    val fingerprint: String?,
    val status: Int?,
    val body: String?,
    val revision: Long?,
)

/**
 * Toda escrita do casal passa por aqui, em uma única transação que grava a alteração, os
 * acontecimentos, a chave e o resultado.
 *
 * Ordem dos bloqueios: a sessão, a chave e o contador. Quem espera por uma chave ainda não detém o
 * contador, e a revogação não pede nenhum dos outros dois, então não há como formar um ciclo.
 */
@Service
class Operations(
    private val jdbc: JdbcClient,
    transactions: PlatformTransactionManager,
    private val mapper: JsonMapper,
    private val identity: Identity,
) {
    private val transaction = TransactionTemplate(transactions)

    /** `content` identifica o comando: a mesma chave só repete o resultado se ele for igual. */
    fun execute(session: Session, key: String, content: List<Any?>, command: (WriteContext) -> CommandResult): StoredResponse {
        if (!isValidOperationKey(key)) return error(400, "IDEMPOTENCY_KEY_INVALID")
        val fingerprint = sha256(mapper.writeValueAsString(content))
        val actor = session.person
        return transaction.execute {
            identity.holdActiveSession(session)
            if (!reserve(actor, key, "COMPLETED", fingerprint)) return@execute existingResult(actor, key, fingerprint)
            // O bloqueio vai até o commit: as revisões ficam visíveis em ordem, e uma leitura nunca pula uma ainda aberta.
            val current = jdbc.sql("SELECT value FROM sync_counter WHERE id = 1 FOR UPDATE").query(Long::class.java).single()
            val context = WriteContext(jdbc, current)
            val result = command(context)
            val response = StoredResponse(result.status, mapper.writeValueAsString(result.body))
            jdbc.sql("""
                UPDATE operation SET http_status = :status, response_body = :body, revision = :revision
                WHERE person = :person AND op_key = :key
            """).param("status", response.status).param("body", response.json).param("revision", context.revision)
                .param("person", actor.name).param("key", key).update()
            response
        }!!
    }

    /** Resposta definitiva: espera uma transação em andamento e só cancela se nada foi confirmado. */
    fun discard(session: Session, key: String): DiscardResult = transaction.execute {
        identity.holdActiveSession(session)
        val actor = session.person
        if (reserve(actor, key, "CANCELLED", null)) return@execute DiscardResult(DiscardOutcome.CANCELLED, null)
        val row = load(actor, key)
        when {
            row.state == "CANCELLED" -> DiscardResult(DiscardOutcome.CANCELLED, null)
            row.status!! in 200..299 -> DiscardResult(DiscardOutcome.CONFIRMED, row.revision)
            else -> DiscardResult(DiscardOutcome.REFUSED, null)
        }
    }!!

    private fun reserve(actor: Person, key: String, state: String, fingerprint: String?): Boolean = jdbc.sql("""
        INSERT INTO operation (person, op_key, state, fingerprint) VALUES (:person, :key, :state, :fingerprint)
        ON CONFLICT (person, op_key) DO NOTHING
    """).param("person", actor.name).param("key", key).param("state", state)
        .param("fingerprint", fingerprint, java.sql.Types.VARCHAR).update() == 1

    private fun existingResult(actor: Person, key: String, fingerprint: String): StoredResponse {
        val row = load(actor, key)
        return when {
            row.state == "CANCELLED" -> error(409, "OPERATION_CANCELLED")
            row.fingerprint != fingerprint -> error(409, "IDEMPOTENCY_KEY_REUSED")
            else -> StoredResponse(row.status!!, row.body!!)
        }
    }

    private fun load(actor: Person, key: String): OperationRow = jdbc.sql(
        "SELECT state, fingerprint, http_status, response_body, revision FROM operation WHERE person = :person AND op_key = :key",
    ).param("person", actor.name).param("key", key).query { rs, _ ->
        OperationRow(
            rs.getString("state"),
            rs.getString("fingerprint"),
            rs.getObject("http_status", Integer::class.java)?.toInt(),
            rs.getString("response_body"),
            rs.getObject("revision", java.lang.Long::class.java)?.toLong(),
        )
    }.single()

    private fun error(status: Int, code: String) = StoredResponse(status, mapper.writeValueAsString(ApiError(code)))
}
