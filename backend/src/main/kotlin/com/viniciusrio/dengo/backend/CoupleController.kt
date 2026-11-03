package com.viniciusrio.dengo.backend

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

data class CreateRequestBody(val type: CareRequestType, val message: String? = null)
data class SetMoodBody(val option: MoodOption)

private fun StoredResponse.toEntity(): ResponseEntity<String> =
    ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(json)

@RestController
class CoupleController(private val operations: Operations, private val commands: CoupleCommands) {
    @PostMapping("/v1/requests")
    fun createRequest(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
        @RequestBody body: CreateRequestBody,
    ) = operations.execute(session, key, listOf("createRequest", body.type, body.message)) { context ->
        commands.createRequest(context, session.person, body.type, body.message)
    }.toEntity()

    @PostMapping("/v1/requests/{id}/accept")
    fun accept(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
        @PathVariable id: Long,
    ) = operations.execute(session, key, listOf("accept", id)) { commands.acceptRequest(it, session.person, id) }.toEntity()

    @PostMapping("/v1/requests/{id}/decline")
    fun decline(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
        @PathVariable id: Long,
    ) = operations.execute(session, key, listOf("decline", id)) { commands.declineRequest(it, session.person, id) }.toEntity()

    @PostMapping("/v1/requests/{id}/acknowledge")
    fun acknowledge(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
        @PathVariable id: Long,
    ) = operations.execute(session, key, listOf("acknowledge", id)) { commands.acknowledgeRequest(it, session.person, id) }.toEntity()

    @PutMapping("/v1/mood")
    fun setMood(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
        @RequestBody body: SetMoodBody,
    ) = operations.execute(session, key, listOf("setMood", body.option)) { commands.setMood(it, session.person, body.option) }.toEntity()

    @PostMapping("/v1/personal-space")
    fun activatePersonalSpace(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
    ) = operations.execute(session, key, listOf("activatePersonalSpace")) { commands.activatePersonalSpace(it, session.person) }.toEntity()

    @DeleteMapping("/v1/personal-space")
    fun endPersonalSpace(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @RequestHeader("Idempotency-Key") key: String,
    ) = operations.execute(session, key, listOf("endPersonalSpace")) { commands.endPersonalSpace(it, session.person) }.toEntity()

    @DeleteMapping("/v1/operations/{key}")
    fun discard(
        @RequestAttribute(SESSION_ATTRIBUTE) session: Session,
        @PathVariable key: String,
    ): ResponseEntity<Any> {
        if (!isValidOperationKey(key)) return ResponseEntity.badRequest().body(ApiError("IDEMPOTENCY_KEY_INVALID"))
        return ResponseEntity.ok(operations.discard(session, key))
    }
}
