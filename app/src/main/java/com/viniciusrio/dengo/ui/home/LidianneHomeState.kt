package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.PersonalSpace

data class LidianneHomeState(
    val latestRequest: CareRequest?,
    val mood: Mood?,
    val personalSpace: PersonalSpace,
    val otherRespondedRequestCount: Int = 0,
)
