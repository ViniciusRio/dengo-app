package com.viniciusrio.dengo.model

data class CoupleState(
    val partners: List<Partner>,
    val requests: List<CareRequest> = emptyList(),
    val mood: Mood? = null,
    val personalSpace: PersonalSpace = PersonalSpace(),
    val history: List<HistoryEvent> = emptyList(),
)
