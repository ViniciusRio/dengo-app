package com.viniciusrio.dengo.data.sync

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.viniciusrio.dengo.data.local.PreservingOpenHelperFactory
import java.io.File

/** A cópia do servidor. O `dengo-local.db` antigo fica intacto e nunca é aberto. */
internal const val SYNC_DATABASE_NAME = "dengo-sync.db"

// IDs vindos do servidor, sem geração local.
@Entity(tableName = "requests")
internal data class SyncRequestRow(
    @PrimaryKey val id: Long,
    val type: String,
    val requesterId: String,
    val recipientId: String,
    val status: String,
    val createdAtIso: String,
    val message: String?,
)

@Entity(tableName = "history")
internal data class SyncHistoryRow(
    @PrimaryKey val id: Long,
    val kind: String,
    val requestId: Long?,
    val moodOption: String?,
    val actorId: String,
    val occurredAtIso: String,
)

@Entity(tableName = "mood")
internal data class SyncMoodRow(@PrimaryKey val id: Int = 1, val partnerId: String, val option: String, val dateEpochDay: Long)

@Entity(tableName = "personal_space")
internal data class SyncPersonalSpaceRow(@PrimaryKey val id: Int = 1, val status: String, val activatedAtIso: String?)

@Entity(tableName = "cursor")
internal data class SyncCursorRow(@PrimaryKey val id: Int = 1, val revision: Long)

/** A ordem de chegada é a do `rowid`: o upsert atualiza no lugar e não a muda. */
@Entity(tableName = "pending_operations")
internal data class PendingOperationRow(@PrimaryKey val opKey: String, val command: String, val confirmedRevision: Long?)

@Dao
internal interface SyncDao {
    @Query("SELECT * FROM requests ORDER BY id") suspend fun requests(): List<SyncRequestRow>
    @Query("SELECT * FROM history ORDER BY id") suspend fun history(): List<SyncHistoryRow>
    @Query("SELECT * FROM mood WHERE id = 1") suspend fun mood(): SyncMoodRow?
    @Query("SELECT * FROM personal_space WHERE id = 1") suspend fun personalSpace(): SyncPersonalSpaceRow?
    @Query("SELECT revision FROM cursor WHERE id = 1") suspend fun cursor(): Long?
    @Query("SELECT * FROM pending_operations ORDER BY rowid") suspend fun pending(): List<PendingOperationRow>

    @Upsert suspend fun putRequests(rows: List<SyncRequestRow>)
    @Upsert suspend fun putHistory(rows: List<SyncHistoryRow>)
    @Upsert suspend fun putMood(row: SyncMoodRow)
    @Upsert suspend fun putPersonalSpace(row: SyncPersonalSpaceRow)
    @Upsert suspend fun putCursor(row: SyncCursorRow)
    @Upsert suspend fun putPending(row: PendingOperationRow)

    @Query("DELETE FROM pending_operations WHERE opKey = :key") suspend fun deletePending(key: String)

    @Query("DELETE FROM requests") suspend fun deleteRequests()
    @Query("DELETE FROM history") suspend fun deleteHistory()
    @Query("DELETE FROM mood") suspend fun deleteMood()
    @Query("DELETE FROM personal_space") suspend fun deletePersonalSpace()
    @Query("DELETE FROM cursor") suspend fun deleteCursor()
    @Query("DELETE FROM pending_operations") suspend fun deleteAllPending()

    suspend fun deleteEverything() {
        deleteRequests(); deleteHistory(); deleteMood(); deletePersonalSpace(); deleteCursor(); deleteAllPending()
    }
}

@Database(
    entities = [
        SyncRequestRow::class,
        SyncHistoryRow::class,
        SyncMoodRow::class,
        SyncPersonalSpaceRow::class,
        SyncCursorRow::class,
        PendingOperationRow::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class SyncDatabase : RoomDatabase() {
    abstract fun dao(): SyncDao

    companion object {
        fun open(context: Context, name: String): SyncDatabase =
            Room.databaseBuilder(context, SyncDatabase::class.java, name)
                .openHelperFactory(PreservingOpenHelperFactory())
                .build()
    }
}

/**
 * Apaga os bancos por pessoa da etapa 3, que só tiveram dados de teste e ficaram sem uso com a cópia
 * sincronizada. Não toca no `dengo-local.db` nem em outro arquivo.
 */
internal fun deleteStageThreeSessionDatabases(databaseDirectory: File) {
    val obsolete = Regex("dengo-session-(lidianne|vinicius)\\.db(-wal|-shm|-journal)?")
    databaseDirectory.listFiles()?.filter { obsolete.matches(it.name) }?.forEach(File::delete)
}
