package com.viniciusrio.dengo.backend

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

data class CreateRequestBody(val type: CareRequestType, val message: String? = null)

private fun StoredResponse.toEntity(): ResponseEntity<String> =
    ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(json)

@RestController
class CoupleController(private val operations: Operations, private val commands: CoupleCommands) {
    @PostMapping("/v1/requests")
    fun createRequest(
        @RequestAttribute(ACTOR_ATTRIBUTE) actor: Person,
        @RequestHeader("Idempotency-Key") key: String,
        @RequestBody body: CreateRequestBody,
    ): ResponseEntity<String> = operations.execute { context ->
        commands.createRequest(context, actor, body.type, body.message)
    }.toEntity()

    @PostMapping("/v1/requests/{id}/accept")
    fun accept(
        @RequestAttribute(ACTOR_ATTRIBUTE) actor: Person,
        @RequestHeader("Idempotency-Key") key: String,
        @PathVariable id: Long,
    ): ResponseEntity<String> = operations.execute { context -> commands.acceptRequest(context, actor, id) }.toEntity()

    @PostMapping("/v1/requests/{id}/decline")
    fun decline(
        @RequestAttribute(ACTOR_ATTRIBUTE) actor: Person,
        @RequestHeader("Idempotency-Key") key: String,
        @PathVariable id: Long,
    ): ResponseEntity<String> = operations.execute { context -> commands.declineRequest(context, actor, id) }.toEntity()

    @PostMapping("/v1/requests/{id}/acknowledge")
    fun acknowledge(
        @RequestAttribute(ACTOR_ATTRIBUTE) actor: Person,
        @RequestHeader("Idempotency-Key") key: String,
        @PathVariable id: Long,
    ): ResponseEntity<String> = operations.execute { context -> commands.acknowledgeRequest(context, actor, id) }.toEntity()

    @PostMapping("/v1/personal-space")
    fun activatePersonalSpace(
        @RequestAttribute(ACTOR_ATTRIBUTE) actor: Person,
        @RequestHeader("Idempotency-Key") key: String,
    ): ResponseEntity<String> = operations.execute { context -> commands.activatePersonalSpace(context, actor) }.toEntity()
}
