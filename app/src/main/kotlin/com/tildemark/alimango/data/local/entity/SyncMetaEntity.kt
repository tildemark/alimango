package com.tildemark.alimango.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_meta")
data class SyncMetaEntity(
    @PrimaryKey val resourceType: String, // e.g. "subjects" or "assignments"
    val lastSyncedAt: String? // ISO-8601 string or null
)
