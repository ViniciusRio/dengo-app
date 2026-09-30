package com.lidiannevinicius.dengo.model

import java.time.LocalDate

data class Mood(
    val option: MoodOption,
    val date: LocalDate,
)
