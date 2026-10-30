package com.viniciusrio.dengo.lab

import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class SignalControllerTest {
    private val eventId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

    @Test fun acceptsOnlyConfirmedRoutedPublication() {
        val published = mutableListOf<UUID>()
        val controller = SignalController { id -> published += id; true }
        val response = controller.publish(mapOf("eventId" to eventId.toString()))
        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertEquals(mapOf("eventId" to eventId.toString()), response.body)
        assertEquals(listOf(eventId), published)
    }

    @Test fun rejectsInvalidOrExtraFieldsWithoutPublishing() {
        var count = 0
        val controller = SignalController { count++; true }
        for (body in listOf(emptyMap(), mapOf("eventId" to "bad"), mapOf("eventId" to "1-1-1-1-1"), mapOf("eventId" to eventId.toString(), "name" to "x"))) {
            assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException::class.java) { controller.publish(body) }.statusCode)
        }
        assertEquals(0, count)
    }

    @Test fun inconclusivePublicationIsNotAcceptedOrRetried() {
        var count = 0
        val controller = SignalController { count++; false }
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, assertThrows(ResponseStatusException::class.java) {
            controller.publish(mapOf("eventId" to eventId.toString()))
        }.statusCode)
        assertEquals(1, count)
    }
}
