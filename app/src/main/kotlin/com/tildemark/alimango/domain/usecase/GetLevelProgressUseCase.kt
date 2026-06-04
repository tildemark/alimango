package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import android.util.Log
import javax.inject.Inject

data class LevelProgress(
    val currentLevel: Int,
    val items: List<LevelSubjectProgress>,
    val radicalsPassed: Int,
    val radicalsTotal: Int,
    val kanjiPassed: Int,
    val kanjiTotal: Int,
    val vocabPassed: Int,
    val vocabTotal: Int
)

data class LevelSubjectProgress(
    val subject: Subject,
    val srsStage: Int?,
    val isPassed: Boolean
)

class GetLevelProgressUseCase @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val assignmentRepository: AssignmentRepository
) {
    operator fun invoke(level: Int): Flow<LevelProgress> {
        return combine(
            subjectRepository.observeSubjectsByLevel(level),
            assignmentRepository.observeAllAssignments(level)
        ) { subjects, assignments ->
            Log.d("GetLevelProgressUseCase", "invoke level=$level, subjects=${subjects.size}, assignments=${assignments.size}")
            val progressItems = subjects
                .filter { it.type == "radical" || it.type == "kanji" || it.type == "vocabulary" || it.type == "kana_vocabulary" }
                .map { subject ->
                    val assignment = assignments.find { it.subjectId == subject.id }
                    val srsStage = assignment?.srsStage
                    // WaniKani considers an item passed when it reaches Guru stage (5 or above)
                    val isPassed = srsStage != null && srsStage >= 5
                    LevelSubjectProgress(subject, srsStage, isPassed)
                }

            val radicals = progressItems.filter { it.subject.type == "radical" }
            val kanji = progressItems.filter { it.subject.type == "kanji" }
            val vocab = progressItems.filter { it.subject.type == "vocabulary" || it.subject.type == "kana_vocabulary" }

            LevelProgress(
                currentLevel = level,
                items = progressItems,
                radicalsPassed = radicals.count { it.isPassed },
                radicalsTotal = radicals.size,
                kanjiPassed = kanji.count { it.isPassed },
                kanjiTotal = kanji.size,
                vocabPassed = vocab.count { it.isPassed },
                vocabTotal = vocab.size
            )
        }
    }
}
