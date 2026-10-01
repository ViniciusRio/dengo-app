package com.viniciusrio.dengo.model

import java.time.Instant

data class MuralNote(
    val id: Long,
    val authorId: PartnerId,
    val text: String,
    val createdAt: Instant,
)

const val MURAL_NOTE_MAX_CODE_POINTS = 160

fun String.muralCodePointCount(): Int = codePointCount(0, length)

fun List<MuralNote>.latestMuralNote(): MuralNote? = maxWithOrNull(
    compareBy<MuralNote> { it.createdAt }.thenBy { it.id },
)
