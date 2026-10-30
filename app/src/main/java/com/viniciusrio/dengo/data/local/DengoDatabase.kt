package com.viniciusrio.dengo.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

@Dao
internal interface DengoDao {
    @Query("SELECT * FROM care_requests ORDER BY id")
    suspend fun requests(): List<CareRequestRow>

    @Query("SELECT * FROM history_events ORDER BY id")
    suspend fun history(): List<HistoryEventRow>

    @Query("SELECT * FROM current_mood WHERE id = 1")
    suspend fun mood(): MoodRow?

    @Query("SELECT * FROM personal_space WHERE id = 1")
    suspend fun personalSpace(): PersonalSpaceRow?

    @Query("SELECT * FROM mural_notes ORDER BY id")
    suspend fun muralNotes(): List<MuralNoteRow>

    @Query("SELECT * FROM drawing_strokes ORDER BY noteId, position")
    suspend fun drawingStrokes(): List<DrawingStrokeRow>

    @Query("SELECT * FROM drawing_points ORDER BY noteId, strokePosition, position")
    suspend fun drawingPoints(): List<DrawingPointRow>

    @Insert
    suspend fun insertRequest(row: CareRequestRow): Long

    @Query("UPDATE care_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: Long, status: String): Int

    @Insert
    suspend fun insertHistory(row: HistoryEventRow): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putMood(row: MoodRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putPersonalSpace(row: PersonalSpaceRow)

    @Insert
    suspend fun insertMuralNote(row: MuralNoteRow): Long

    @Insert
    suspend fun insertStrokes(rows: List<DrawingStrokeRow>)

    @Insert
    suspend fun insertPoints(rows: List<DrawingPointRow>)
}

@Database(
    entities = [
        CareRequestRow::class,
        HistoryEventRow::class,
        MoodRow::class,
        PersonalSpaceRow::class,
        MuralNoteRow::class,
        DrawingStrokeRow::class,
        DrawingPointRow::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class DengoDatabase : RoomDatabase() {
    abstract fun dao(): DengoDao

    companion object {
        fun open(context: Context, name: String = "dengo-local.db"): DengoDatabase =
            Room.databaseBuilder(context, DengoDatabase::class.java, name)
                .openHelperFactory(PreservingOpenHelperFactory())
                .build()
    }
}

private class PreservingOpenHelperFactory : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        val original = configuration.callback
        val callback = object : SupportSQLiteOpenHelper.Callback(original.version) {
            override fun onConfigure(db: SupportSQLiteDatabase) = original.onConfigure(db)
            override fun onCreate(db: SupportSQLiteDatabase) = original.onCreate(db)
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                original.onUpgrade(db, oldVersion, newVersion)
            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
                original.onDowngrade(db, oldVersion, newVersion)
            override fun onOpen(db: SupportSQLiteDatabase) = original.onOpen(db)

            // O callback padrão apaga o banco. A falha deve chegar ao repository para ReadError.
            override fun onCorruption(db: SupportSQLiteDatabase) = Unit
        }
        val preserving = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
            .name(configuration.name)
            .callback(callback)
            .noBackupDirectory(configuration.useNoBackupDirectory)
            .allowDataLossOnRecovery(false)
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(preserving)
    }
}
