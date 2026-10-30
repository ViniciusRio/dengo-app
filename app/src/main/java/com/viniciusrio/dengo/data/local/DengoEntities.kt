package com.viniciusrio.dengo.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "care_requests")
internal data class CareRequestRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val requesterId: String,
    val recipientId: String,
    val status: String,
    val createdAtIso: String,
    val message: String?,
)

@Entity(tableName = "history_events", indices = [Index("requestId")])
internal data class HistoryEventRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val requestId: Long?,
    val moodOption: String?,
    val actorId: String,
    val occurredAtIso: String,
)

@Entity(tableName = "current_mood")
internal data class MoodRow(
    @PrimaryKey val id: Int = 1,
    val partnerId: String,
    val option: String,
    val dateEpochDay: Long,
)

@Entity(tableName = "personal_space")
internal data class PersonalSpaceRow(
    @PrimaryKey val id: Int = 1,
    val status: String,
    val activatedAtIso: String?,
)

@Entity(tableName = "mural_notes")
internal data class MuralNoteRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val kind: String,
    val text: String?,
    val aspectRatio: Float?,
    val createdAtIso: String,
)

@Entity(
    tableName = "drawing_strokes",
    primaryKeys = ["noteId", "position"],
    foreignKeys = [ForeignKey(
        entity = MuralNoteRow::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("noteId")],
)
internal data class DrawingStrokeRow(
    val noteId: Long,
    val position: Int,
    val argb: Int,
    val aspectRatio: Float?,
)

@Entity(
    tableName = "drawing_points",
    primaryKeys = ["noteId", "strokePosition", "position"],
    foreignKeys = [ForeignKey(
        entity = DrawingStrokeRow::class,
        parentColumns = ["noteId", "position"],
        childColumns = ["noteId", "strokePosition"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["noteId", "strokePosition"])],
)
internal data class DrawingPointRow(
    val noteId: Long,
    val strokePosition: Int,
    val position: Int,
    val x: Float,
    val y: Float,
)
