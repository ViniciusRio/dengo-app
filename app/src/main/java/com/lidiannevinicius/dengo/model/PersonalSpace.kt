package com.lidiannevinicius.dengo.model

import java.time.Instant

data class PersonalSpace(
    val isActive: Boolean = false,
    val activatedAt: Instant? = null,
)
