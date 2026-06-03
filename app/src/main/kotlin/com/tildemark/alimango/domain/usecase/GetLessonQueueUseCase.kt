package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.SubjectRepository
import javax.inject.Inject

data class LessonItem(
    val assignment: Assignment,
    val subject: Subject
)

class GetLessonQueueUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(limit: Int = 5): List<LessonItem> {
        val lessons = assignmentRepository.getAvailableLessons().take(limit)
        if (lessons.isEmpty()) return emptyList()

        val subjectIds = lessons.map { it.subjectId }
        val subjects = subjectRepository.getSubjectsByIds(subjectIds)

        return lessons.mapNotNull { assignment ->
            val subject = subjects.find { it.id == assignment.subjectId }
            if (subject != null) LessonItem(assignment, subject) else null
        }
    }
}
