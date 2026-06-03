package com.tildemark.alimango.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentDto(
    @SerialName("created_at") val createdAt: String,
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("subject_type") val subjectType: String,
    @SerialName("srs_stage") val srsStage: Int,
    @SerialName("unlocked_at") val unlockedAt: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("passed_at") val passedAt: String? = null,
    @SerialName("burned_at") val burnedAt: String? = null,
    @SerialName("available_at") val availableAt: String? = null,
    @SerialName("resurrected_at") val resurrectedAt: String? = null,
    @SerialName("user_synonyms") val userSynonyms: List<String> = emptyList()
)
