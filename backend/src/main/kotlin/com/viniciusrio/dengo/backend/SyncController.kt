package com.viniciusrio.dengo.backend

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SyncController(private val reader: SyncReader) {
    @GetMapping("/v1/sync")
    fun sync(
        @RequestParam(defaultValue = "0") after: Long,
        @RequestParam(defaultValue = "100") limit: Int,
    ): ResponseEntity<Any> {
        if (after < 0 || limit !in 1..500) return ResponseEntity.badRequest().body(ApiError("MALFORMED_REQUEST"))
        return ResponseEntity.ok(reader.read(after, limit))
    }
}
