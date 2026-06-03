package com.tildemark.alimango.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    indices = [Index(value = ["subjectId"], unique = true)]
)
data class AssignmentEntity(
    @PrimaryKey val id: Int,
    val subjectId: Int,
    val subjectType: String,
    val srsStage: Int,
    val unlockedAt: String?, // ISO-8601 string or null
    val availableAt: String?, // ISO-8601 string or null
    val burnedAt: String?,
    val startedAt: String?,
    val passedAt: String?,
    val userSynonyms: String = "", // Comma-separated user synonyms
    val note: String = "" // User note
)
