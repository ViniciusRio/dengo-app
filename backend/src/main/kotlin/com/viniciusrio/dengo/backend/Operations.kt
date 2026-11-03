package com.viniciusrio.dengo.backend

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.json.JsonMapper

data class CommandResult(val status: Int, val body: Any)
data class StoredResponse(val status: Int, val json: String)

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

/** Toda escrita do casal passa por aqui, em uma única transação com seus acontecimentos. */
@Service
class Operations(
    private val jdbc: JdbcClient,
    transactions: PlatformTransactionManager,
    private val mapper: JsonMapper,
) {
    private val transaction = TransactionTemplate(transactions)

    fun execute(command: (WriteContext) -> CommandResult): StoredResponse = transaction.execute {
        // O bloqueio vai até o commit: as revisões ficam visíveis em ordem, e uma leitura nunca pula uma ainda aberta.
        val current = jdbc.sql("SELECT value FROM sync_counter WHERE id = 1 FOR UPDATE").query(Long::class.java).single()
        val result = command(WriteContext(jdbc, current))
        StoredResponse(result.status, mapper.writeValueAsString(result.body))
    }!!
}
