package com.lidiannevinicius.tamagotchi.ui.home

import com.lidiannevinicius.tamagotchi.model.CareRequest
import com.lidiannevinicius.tamagotchi.model.Mood
import com.lidiannevinicius.tamagotchi.model.PersonalSpace

data class LidianneHomeState(
    val latestRequest: CareRequest?,
    val mood: Mood?,
    val personalSpace: PersonalSpace,
)
