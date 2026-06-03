package com.tildemark.alimango.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("username") val username: String,
    @SerialName("level") val level: Int,
    @SerialName("profile_url") val profileUrl: String,
    @SerialName("started_at") val startedAt: String
)
