package com.viniciusrio.dengo.backend

import java.sql.Types
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service

enum class CareRequestType { DENGO, HOT_WATER_BAG, MEDICINE, SPEND_TIME_TOGETHER, OTHER }
enum class CareRequestStatus { PENDING, ACCEPTED, ACKNOWLEDGED, DECLINED }
enum class MoodOption { VERY_HAPPY, HAPPY, OKAY, SAD, UPSET, TIRED, LOVING, NEEDY }

data class CreateRequestResult(val changed: Boolean, val revision: Long, val request: RequestView)
data class ChangeResult(val changed: Boolean, val revision: Long)

private fun refused(status: Int, code: String) = CommandResult(status, ApiError(code))
private val notAllowed = refused(403, "NOT_ALLOWED_FOR_PERSON")
private val notFound = refused(404, "REQUEST_NOT_FOUND")
private val invalidTransition = refused(409, "INVALID_TRANSITION")
private val invalidRequest = refused(422, "INVALID_REQUEST")

/**
 * Regras portadas de `RoomCoupleRepository`. Quem age vem sempre da sessão, e cada comando roda
 * com o bloqueio de escrita: o que ele lê não muda até o commit.
 */
@Service
class CoupleCommands(
    private val jdbc: JdbcClient,
    private val clock: Clock,
    @Value("\${dengo.mood-zone}") private val moodZone: ZoneId,
) {
    fun createRequest(context: WriteContext, actor: Person, type: CareRequestType, message: String?): CommandResult {
        if (actor != Person.LIDIANNE) return notAllowed
        if (type != CareRequestType.OTHER && message != null) return invalidRequest
        val normalized = message?.trim()?.takeIf(String::isNotEmpty)
        if (type == CareRequestType.OTHER && normalized == null) return invalidRequest
        if (type != CareRequestType.OTHER) {
            activeRequest(type)?.let { return CommandResult(200, CreateRequestResult(false, context.revision, it)) }
        }
        val revision = context.nextRevision()
        val now = clock.now()
        val id = jdbc.sql("""
            INSERT INTO care_request (type, requester_id, recipient_id, status, created_at, message, revision)
            VALUES (:type, :requester, :recipient, :status, :now, :message, :revision) RETURNING id
        """).param("type", type.name)
            .param("requester", Person.LIDIANNE.name)
            .param("recipient", Person.VINICIUS.name)
            .param("status", CareRequestStatus.PENDING.name)
            .param("now", now.toDb())
            .param("message", normalized, Types.VARCHAR)
            .param("revision", revision)
            .query(Long::class.java).single()
        recordEvent("REQUEST_CREATED", actor, now, revision, requestId = id)
        val view = RequestView(id, type, Person.LIDIANNE, Person.VINICIUS, CareRequestStatus.PENDING, now, normalized, revision)
        return CommandResult(200, CreateRequestResult(true, revision, view))
    }

    fun acceptRequest(context: WriteContext, actor: Person, id: Long) =
        respond(context, actor, id, CareRequestStatus.ACCEPTED, "REQUEST_ACCEPTED")

    fun declineRequest(context: WriteContext, actor: Person, id: Long) =
        respond(context, actor, id, CareRequestStatus.DECLINED, "REQUEST_DECLINED")

    private fun respond(context: WriteContext, actor: Person, id: Long, status: CareRequestStatus, event: String): CommandResult {
        if (actor != Person.VINICIUS) return notAllowed
        val current = requestStatus(id) ?: return notFound
        if (personalSpaceActive()) return refused(409, "PERSONAL_SPACE_ACTIVE")
        if (current != CareRequestStatus.PENDING) return invalidTransition
        return transition(context, actor, id, status, event)
    }

    fun acknowledgeRequest(context: WriteContext, actor: Person, id: Long): CommandResult {
        if (actor != Person.LIDIANNE) return notAllowed
        val current = requestStatus(id) ?: return notFound
        if (current != CareRequestStatus.ACCEPTED) return invalidTransition
        return transition(context, actor, id, CareRequestStatus.ACKNOWLEDGED, "REQUEST_ACKNOWLEDGED")
    }

    fun setMood(context: WriteContext, actor: Person, option: MoodOption): CommandResult {
        if (actor != Person.LIDIANNE) return notAllowed
        val now = clock.now()
        val date = LocalDate.ofInstant(now, moodZone)
        val unchanged = jdbc.sql("SELECT count(*) FROM mood WHERE partner_id = :person AND option = :option AND mood_date = :date")
            .param("person", actor.name).param("option", option.name).param("date", date)
            .query(Int::class.java).single() > 0
        if (unchanged) return CommandResult(200, ChangeResult(false, context.revision))
        val revision = context.nextRevision()
        jdbc.sql("""
            INSERT INTO mood (partner_id, option, mood_date, revision) VALUES (:person, :option, :date, :revision)
            ON CONFLICT (partner_id) DO UPDATE SET option = :option, mood_date = :date, revision = :revision
        """).param("person", actor.name).param("option", option.name).param("date", date).param("revision", revision).update()
        recordEvent("MOOD_CHANGED", actor, now, revision, moodOption = option)
        return CommandResult(200, ChangeResult(true, revision))
    }

    fun activatePersonalSpace(context: WriteContext, actor: Person): CommandResult {
        if (actor != Person.LIDIANNE) return notAllowed
        if (personalSpaceActive()) return CommandResult(200, ChangeResult(false, context.revision))
        val revision = context.nextRevision()
        val now = clock.now()
        jdbc.sql("UPDATE personal_space SET status = 'ACTIVE', activated_at = :now, revision = :revision WHERE id = 1")
            .param("now", now.toDb()).param("revision", revision).update()
        recordEvent("PERSONAL_SPACE_ACTIVATED", actor, now, revision)
        return CommandResult(200, ChangeResult(true, revision))
    }

    fun endPersonalSpace(context: WriteContext, actor: Person): CommandResult {
        if (actor != Person.LIDIANNE) return notAllowed
        if (!personalSpaceActive()) return CommandResult(200, ChangeResult(false, context.revision))
        val revision = context.nextRevision()
        jdbc.sql("UPDATE personal_space SET status = 'INACTIVE', activated_at = NULL, revision = :revision WHERE id = 1")
            .param("revision", revision).update()
        recordEvent("PERSONAL_SPACE_ENDED", actor, clock.now(), revision)
        return CommandResult(200, ChangeResult(true, revision))
    }

    private fun transition(context: WriteContext, actor: Person, id: Long, status: CareRequestStatus, event: String): CommandResult {
        val revision = context.nextRevision()
        jdbc.sql("UPDATE care_request SET status = :status, revision = :revision WHERE id = :id")
            .param("status", status.name).param("revision", revision).param("id", id).update()
        recordEvent(event, actor, clock.now(), revision, requestId = id)
        return CommandResult(200, ChangeResult(true, revision))
    }

    private fun activeRequest(type: CareRequestType): RequestView? = jdbc.sql(
        "SELECT * FROM care_request WHERE type = :type AND status IN ('PENDING', 'ACCEPTED')",
    ).param("type", type.name).query { rs, _ -> rs.toRequestView() }.optional().orElse(null)

    private fun requestStatus(id: Long): CareRequestStatus? = jdbc.sql("SELECT status FROM care_request WHERE id = :id")
        .param("id", id).query(String::class.java).optional().map(CareRequestStatus::valueOf).orElse(null)

    private fun personalSpaceActive() = jdbc.sql("SELECT status FROM personal_space WHERE id = 1")
        .query(String::class.java).single() == "ACTIVE"

    private fun recordEvent(type: String, actor: Person, at: Instant, revision: Long, requestId: Long? = null, moodOption: MoodOption? = null) {
        jdbc.sql("""
            INSERT INTO history_event (type, actor_id, occurred_at, request_id, mood_option, revision)
            VALUES (:type, :actor, :at, :request, :option, :revision)
        """).param("type", type).param("actor", actor.name).param("at", at.toDb())
            .param("request", requestId, Types.BIGINT).param("option", moodOption?.name, Types.VARCHAR)
            .param("revision", revision).update()
    }
}
