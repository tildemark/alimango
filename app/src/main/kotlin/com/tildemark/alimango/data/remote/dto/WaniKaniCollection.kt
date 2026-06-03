package com.tildemark.alimango.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WaniKaniCollection<T>(
    @SerialName("object") val objectType: String,
    @SerialName("url") val url: String,
    @SerialName("pages") val pages: Pages,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("data_updated_at") val dataUpdatedAt: String?,
    @SerialName("data") val data: List<WaniKaniResource<T>>
)

@Serializable
data class WaniKaniResource<T>(
    @SerialName("id") val id: Int = 0,
    @SerialName("object") val objectType: String,
    @SerialName("url") val url: String,
    @SerialName("data_updated_at") val dataUpdatedAt: String? = null,
    @SerialName("data") val data: T
)

@Serializable
data class Pages(
    @SerialName("per_page") val perPage: Int,
    @SerialName("next_url") val nextUrl: String?,
    @SerialName("previous_url") val previousUrl: String?
)
