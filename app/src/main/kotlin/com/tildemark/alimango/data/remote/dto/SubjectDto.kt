package com.tildemark.alimango.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectDto(
    @SerialName("created_at") val createdAt: String,
    @SerialName("level") val level: Int,
    @SerialName("slug") val slug: String,
    @SerialName("document_url") val documentUrl: String,
    @SerialName("characters") val characters: String? = null,
    @SerialName("meanings") val meanings: List<MeaningDto> = emptyList(),
    @SerialName("readings") val readings: List<ReadingDto> = emptyList(),
    @SerialName("character_images") val characterImages: List<CharacterImageDto> = emptyList(),
    @SerialName("parts_of_speech") val partsOfSpeech: List<String> = emptyList(),
    @SerialName("pronunciation_audios") val pronunciationAudios: List<AudioDto> = emptyList(),
    @SerialName("meaning_mnemonic") val meaningMnemonic: String = "",
    @SerialName("reading_mnemonic") val readingMnemonic: String? = null
)

@Serializable
data class MeaningDto(
    @SerialName("meaning") val meaning: String,
    @SerialName("primary") val isPrimary: Boolean,
    @SerialName("accepted_answer") val acceptedAnswer: Boolean = true
)

@Serializable
data class ReadingDto(
    @SerialName("reading") val reading: String,
    @SerialName("primary") val isPrimary: Boolean,
    @SerialName("accepted_answer") val acceptedAnswer: Boolean = true,
    @SerialName("type") val type: String? = null // onyomi, kunyomi, nanori
)

@Serializable
data class CharacterImageDto(
    @SerialName("url") val url: String,
    @SerialName("content_type") val contentType: String
)

@Serializable
data class AudioDto(
    @SerialName("url") val url: String,
    @SerialName("content_type") val contentType: String,
    @SerialName("metadata") val metadata: AudioMetadataDto? = null
)

@Serializable
data class AudioMetadataDto(
    @SerialName("gender") val gender: String? = null,
    @SerialName("source_id") val sourceId: Int? = null,
    @SerialName("pronunciation") val pronunciation: String? = null,
    @SerialName("voice_actor_id") val voiceActorId: Int? = null,
    @SerialName("voice_actor_name") val voiceActorName: String? = null,
    @SerialName("voice_description") val voiceDescription: String? = null
)
