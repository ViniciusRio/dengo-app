package com.viniciusrio.dengo.lab

import java.time.Clock
import java.time.Instant

enum class CareRequestType {
    DENGO,
    HOT_WATER_BAG,
    MEDICINE,
    SPEND_TIME_TOGETHER,
}

data class LabCareRequest(
    val id: Long,
    val type: CareRequestType,
    val status: String,
    val createdAt: Instant,
)

data class CreateOutcome(val request: LabCareRequest, val created: Boolean)

class CareRequestCatalog(private val clock: Clock) {
    private val requests = mutableListOf<LabCareRequest>()

    @Synchronized
    fun create(type: CareRequestType): CreateOutcome {
        requests.firstOrNull { it.type == type }?.let { return CreateOutcome(it, false) }
        val request = LabCareRequest(
            id = requests.size + 1L,
            type = type,
            status = "PENDING",
            createdAt = clock.instant(),
        )
        requests += request
        return CreateOutcome(request, true)
    }

    @Synchronized
    fun list(): List<LabCareRequest> = requests.toList()
}
