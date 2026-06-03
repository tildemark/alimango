package com.tildemark.alimango.domain.model

data class Subject(
    val id: Int,
    val type: String,
    val level: Int,
    val characters: String?,
    val meanings: List<String>,
    val readings: List<String>,
    val partsOfSpeech: List<String>,
    val audioUrls: List<String>,
    val documentUrl: String,
    val meaningMnemonic: String,
    val readingMnemonic: String? = null
)
