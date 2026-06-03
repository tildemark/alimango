package com.tildemark.alimango.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: Int,
    val type: String,
    val level: Int,
    val characters: String?,
    val slug: String,
    val documentUrl: String,
    val dataJson: String // Full serialized SubjectDto
)
