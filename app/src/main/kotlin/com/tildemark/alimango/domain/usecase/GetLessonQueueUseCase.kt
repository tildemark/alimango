package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.SubjectRepository
import com.tildemark.alimango.domain.repository.UserRepository
import javax.inject.Inject

data class LessonItem(
    val assignment: Assignment,
    val subject: Subject
)

class GetLessonQueueUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(limit: Int = 5, subjectIds: List<Int>? = null): List<LessonItem> {
        if (!subjectIds.isNullOrEmpty()) {
            val assignments = subjectIds.mapNotNull { assignmentRepository.getAssignmentBySubjectId(it) }
            val subjects = subjectRepository.getSubjectsByIds(subjectIds)
            return assignments.mapNotNull { assignment ->
                val subject = subjects.find { it.id == assignment.subjectId }
                if (subject != null) LessonItem(assignment, subject) else null
            }
        }

        val pat = userRepository.getSavedPat() ?: ""
        val user = userRepository.getOrFetchUser(pat)
        val userLevel = user?.level ?: 1

        val lessons = assignmentRepository.getAvailableLessons(userLevel).take(limit)
        if (lessons.isEmpty()) return emptyList()

        val activeSubjectIds = lessons.map { it.subjectId }
        val subjects = subjectRepository.getSubjectsByIds(activeSubjectIds)

        return lessons.mapNotNull { assignment ->
            val subject = subjects.find { it.id == assignment.subjectId }
            if (subject != null) LessonItem(assignment, subject) else null
        }
    }
}
