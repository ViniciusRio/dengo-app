package com.lidiannevinicius.dengo.ui.home

import com.lidiannevinicius.dengo.model.CareRequest
import com.lidiannevinicius.dengo.model.Mood
import com.lidiannevinicius.dengo.model.PersonalSpace

data class LidianneHomeState(
    val latestRequest: CareRequest?,
    val mood: Mood?,
    val personalSpace: PersonalSpace,
)
