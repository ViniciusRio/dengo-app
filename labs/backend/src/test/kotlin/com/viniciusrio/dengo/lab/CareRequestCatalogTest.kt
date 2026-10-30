package com.viniciusrio.dengo.lab

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CareRequestCatalogTest {
    private val now = Instant.parse("2026-10-30T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test fun startsEmptyAndCreatesPendingRequest() {
        val catalog = CareRequestCatalog(clock)

        assertEquals(emptyList<LabCareRequest>(), catalog.list())
        val outcome = catalog.create(CareRequestType.DENGO)

        assertEquals(true, outcome.created)
        assertEquals(1L, outcome.request.id)
        assertEquals(CareRequestType.DENGO, outcome.request.type)
        assertEquals("PENDING", outcome.request.status)
        assertEquals(now, outcome.request.createdAt)
        assertEquals(listOf(outcome.request), catalog.list())
    }

    @Test fun repeatedTypeReturnsExistingRequest() {
        val catalog = CareRequestCatalog(clock)

        val first = catalog.create(CareRequestType.DENGO)
        val second = catalog.create(CareRequestType.DENGO)

        assertEquals(true, first.created)
        assertEquals(false, second.created)
        assertEquals(first.request, second.request)
        assertEquals(listOf(first.request), catalog.list())
    }

    @Test fun differentTypesStayInCreationOrderAndReadDoesNotMutateState() {
        val catalog = CareRequestCatalog(clock)
        val first = catalog.create(CareRequestType.DENGO).request
        val second = catalog.create(CareRequestType.MEDICINE).request

        assertEquals(listOf(1L, 2L), catalog.list().map { it.id })
        assertEquals(listOf(first, second), catalog.list())
        assertEquals(listOf(first, second), catalog.list())
    }

    @Test fun concurrentCreationOfSameTypeReturnsOneRequest() {
        val catalog = CareRequestCatalog(clock)
        val executor = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        try {
            val calls = List(2) {
                executor.submit(Callable {
                    start.await()
                    catalog.create(CareRequestType.MEDICINE)
                })
            }
            start.countDown()
            val outcomes = calls.map { it.get(5, TimeUnit.SECONDS) }

            assertEquals(1, outcomes.count { it.created })
            assertEquals(listOf(1L, 1L), outcomes.map { it.request.id })
            assertEquals(1, catalog.list().size)
        } finally {
            executor.shutdownNow()
        }
    }
}
