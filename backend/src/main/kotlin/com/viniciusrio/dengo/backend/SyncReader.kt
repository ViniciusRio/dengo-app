package com.viniciusrio.dengo.backend

import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

data class RequestView(
    val id: Long,
    val type: CareRequestType,
    val requesterId: Person,
    val recipientId: Person,
    val status: CareRequestStatus,
    val createdAt: Instant,
    val message: String?,
    val revision: Long,
)

data class HistoryView(
    val id: Long,
    val type: String,
    val actorId: Person,
    val occurredAt: Instant,
    val requestId: Long?,
    val option: String?,
    val revision: Long,
)

data class MoodView(val partnerId: Person, val option: String, val date: LocalDate, val revision: Long)
data class PersonalSpaceView(val status: String, val activatedAt: Instant?, val revision: Long)

data class SyncPage(
    val revision: Long,
    val hasMore: Boolean,
    val requests: List<RequestView>,
    val history: List<HistoryView>,
    val mood: MoodView?,
    val personalSpace: PersonalSpaceView?,
)

private fun ResultSet.instant(column: String): Instant? = getObject(column, OffsetDateTime::class.java)?.toInstant()

internal fun ResultSet.toRequestView() = RequestView(
    getLong("id"),
    CareRequestType.valueOf(getString("type")),
    Person.valueOf(getString("requester_id")),
    Person.valueOf(getString("recipient_id")),
    CareRequestStatus.valueOf(getString("status")),
    instant("created_at")!!,
    getString("message"),
    getLong("revision"),
)

@Service
class SyncReader(
    private val jdbc: JdbcClient,
    transactions: PlatformTransactionManager,
    private val identity: Identity,
) {
    // Um só retrato do banco para todas as tabelas da página.
    private val snapshot = TransactionTemplate(transactions).apply {
        isolationLevel = TransactionDefinition.ISOLATION_REPEATABLE_READ
        isReadOnly = true
    }

    /** Autentica no mesmo retrato dos dados: nada confirmado depois de uma revogação sai para aquela sessão. */
    fun read(token: String, after: Long, limit: Int): SyncPage = snapshot.execute {
        identity.sessionForToken(token) ?: throw SessionRefused("SESSION_REVOKED")
        val current = jdbc.sql("SELECT value FROM sync_counter WHERE id = 1").query(Long::class.java).single()
        val (upTo, hasMore) = pageEnd(after, limit, current)
        SyncPage(
            revision = upTo,
            hasMore = hasMore,
            requests = requests(after, upTo),
            history = history(after, upTo),
            mood = mood(after, upTo),
            personalSpace = personalSpace(after, upTo),
        )
    }!!

    /**
     * A página termina na revisão da linha de número `limit`, nunca no meio dela. Cada tabela contribui
     * com no máximo `limit` linhas, pelo índice de revisão, sem percorrer o restante do histórico.
     */
    private fun pageEnd(after: Long, limit: Int, current: Long): Pair<Long, Boolean> {
        val end = jdbc.sql("""
            SELECT revision FROM (
                (SELECT revision FROM care_request WHERE revision > :after ORDER BY revision LIMIT :limit)
                UNION ALL (SELECT revision FROM history_event WHERE revision > :after ORDER BY revision LIMIT :limit)
                UNION ALL (SELECT revision FROM mood WHERE revision > :after ORDER BY revision LIMIT :limit)
                UNION ALL (SELECT revision FROM personal_space WHERE revision > :after ORDER BY revision LIMIT :limit)
            ) changed ORDER BY revision OFFSET :limit - 1 LIMIT 1
        """).param("after", after).param("limit", limit).query(Long::class.java).optional().orElse(null)
            ?: return current to false
        val hasMore = jdbc.sql("""
            SELECT EXISTS (SELECT 1 FROM care_request WHERE revision > :end)
                OR EXISTS (SELECT 1 FROM history_event WHERE revision > :end)
                OR EXISTS (SELECT 1 FROM mood WHERE revision > :end)
                OR EXISTS (SELECT 1 FROM personal_space WHERE revision > :end)
        """).param("end", end).query(Boolean::class.java).single()
        return if (hasMore) end to true else current to false
    }

    private fun requests(after: Long, upTo: Long) = jdbc.sql(
        "SELECT * FROM care_request WHERE revision > :after AND revision <= :upTo ORDER BY revision, id",
    ).param("after", after).param("upTo", upTo).query { rs, _ -> rs.toRequestView() }.list()

    private fun history(after: Long, upTo: Long) = jdbc.sql(
        "SELECT * FROM history_event WHERE revision > :after AND revision <= :upTo ORDER BY revision, id",
    ).param("after", after).param("upTo", upTo).query { rs, _ ->
        HistoryView(
            rs.getLong("id"),
            rs.getString("type"),
            Person.valueOf(rs.getString("actor_id")),
            rs.instant("occurred_at")!!,
            rs.getObject("request_id", java.lang.Long::class.java)?.toLong(),
            rs.getString("mood_option"),
            rs.getLong("revision"),
        )
    }.list()

    private fun mood(after: Long, upTo: Long) = jdbc.sql(
        "SELECT * FROM mood WHERE revision > :after AND revision <= :upTo",
    ).param("after", after).param("upTo", upTo).query { rs, _ ->
        MoodView(Person.valueOf(rs.getString("partner_id")), rs.getString("option"), rs.getObject("mood_date", LocalDate::class.java), rs.getLong("revision"))
    }.optional().orElse(null)

    private fun personalSpace(after: Long, upTo: Long) = jdbc.sql(
        "SELECT * FROM personal_space WHERE revision > :after AND revision <= :upTo",
    ).param("after", after).param("upTo", upTo).query { rs, _ ->
        PersonalSpaceView(rs.getString("status"), rs.instant("activated_at"), rs.getLong("revision"))
    }.optional().orElse(null)
}
