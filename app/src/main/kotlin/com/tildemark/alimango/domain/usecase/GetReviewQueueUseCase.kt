package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ReviewItem(
    val subject: Subject,
    val assignment: Assignment
)

class GetReviewQueueUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke(): Flow<List<ReviewItem>> {
        val currentTimeIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        
        return assignmentRepository.observeAvailableReviews(currentTimeIso).map { assignments ->
            val subjectIds = assignments.map { it.subjectId }
            val subjects = subjectRepository.getSubjectsByIds(subjectIds).associateBy { it.id }
            
            assignments.mapNotNull { assignment ->
                val subject = subjects[assignment.subjectId]
                if (subject != null) {
                    ReviewItem(subject, assignment)
                } else {
                    null
                }
            }
        }
    }
}
