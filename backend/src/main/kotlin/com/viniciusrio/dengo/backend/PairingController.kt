package com.viniciusrio.dengo.backend

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class PairingRequest(val code: String)
data class PairingResponse(val person: Person, val token: String)

@RestController
class PairingController(private val identity: Identity) {
    @PostMapping("/v1/pairings")
    fun pair(@RequestBody request: PairingRequest): ResponseEntity<Any> = when (val outcome = identity.pair(request.code)) {
        is PairingOutcome.Paired -> ResponseEntity.ok(PairingResponse(outcome.person, outcome.token))
        PairingOutcome.InvalidCode -> ResponseEntity.badRequest().body(ApiError("PAIRING_CODE_INVALID"))
        is PairingOutcome.Locked -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiError("PAIRING_LOCKED"))
    }
}
