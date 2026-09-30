package com.viniciusrio.dengo.model

import java.time.Instant

data class PersonalSpace(
    val status: Status = Status.INACTIVE,
    val activatedAt: Instant? = null,
) {
    init {
        require((status == Status.ACTIVE) == (activatedAt != null))
    }

    enum class Status {
        INACTIVE,
        ACTIVE,
    }
}
