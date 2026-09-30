package com.lidiannevinicius.tamagotchi.model

import java.time.Instant

data class CareRequest(
    val id: Long,
    val type: CareRequestType,
    val requesterId: PartnerId,
    val recipientId: PartnerId,
    val status: CareRequestStatus,
    val createdAt: Instant,
    val message: String? = null,
)
