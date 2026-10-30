package com.viniciusrio.dengo.lab

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class RequestController(private val catalog: CareRequestCatalog) {
    @GetMapping("/lab/requests")
    fun list(): List<LabCareRequest> = catalog.list()

    @PostMapping("/lab/requests")
    fun create(@RequestBody body: Map<String, String>): ResponseEntity<LabCareRequest> {
        val type = body.takeIf { it.keys == setOf("type") }
            ?.get("type")
            ?.let { value -> CareRequestType.entries.firstOrNull { it.name == value } }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request type")
        val outcome = catalog.create(type)
        val status = if (outcome.created) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(outcome.request)
    }
}
