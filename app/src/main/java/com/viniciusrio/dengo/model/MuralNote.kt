package com.viniciusrio.dengo.model

import java.time.Instant

data class MuralNote(
    val id: Long,
    val authorId: PartnerId,
    val content: MuralContent,
    val createdAt: Instant,
)

sealed interface MuralContent {
    data class Text(val value: String) : MuralContent
    data class Drawing(val aspectRatio: Float, val strokes: List<DrawingStroke>) : MuralContent
}

data class DrawingPoint(val x: Float, val y: Float)
data class DrawingStroke(val argb: Int, val points: List<DrawingPoint>, val aspectRatio: Float? = null)

const val MURAL_NOTE_MAX_CODE_POINTS = 160

fun String.muralCodePointCount(): Int = codePointCount(0, length)

fun List<MuralNote>.latestMuralNote(): MuralNote? = maxWithOrNull(
    compareBy<MuralNote> { it.createdAt }.thenBy { it.id },
)
