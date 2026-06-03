package com.tildemark.alimango.domain.model

data class Assignment(
    val id: Int,
    val subjectId: Int,
    val subjectType: String,
    val srsStage: Int,
    val unlockedAt: String?,
    val availableAt: String?,
    val burnedAt: String?,
    val startedAt: String?,
    val passedAt: String?,
    val userSynonyms: List<String> = emptyList(),
    val note: String = ""
) {
    val isBurned: Boolean
        get() = burnedAt != null

    val srsStageName: String
        get() = when (srsStage) {
            in 1..4 -> "Apprentice"
            in 5..6 -> "Guru"
            7 -> "Master"
            8 -> "Enlightened"
            9 -> "Burned"
            else -> "Initiate"
        }
}
