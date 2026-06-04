package com.tildemark.alimango.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tildemark.alimango.data.local.entity.SyncMetaEntity

@Dao
interface SyncMetaDao {
    @Query("SELECT lastSyncedAt FROM sync_meta WHERE resourceType = :resourceType")
    suspend fun getLastSyncedAt(resourceType: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMeta(syncMeta: SyncMetaEntity)

    @Query("DELETE FROM sync_meta WHERE resourceType = :resourceType")
    suspend fun deleteSyncMeta(resourceType: String)
}
