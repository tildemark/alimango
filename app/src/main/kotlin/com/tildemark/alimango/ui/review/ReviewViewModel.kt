package com.tildemark.alimango.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.usecase.GetReviewQueueUseCase
import com.tildemark.alimango.domain.usecase.ReviewItem
import com.tildemark.alimango.domain.usecase.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.esnault.wanakana.core.Wanakana
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReviewUiState {
    object Loading : ReviewUiState
    object Empty : ReviewUiState
    data class Active(
        val currentItem: ReviewItem,
        val questionType: QuestionType, // Meaning or Reading
        val progress: Float,
        val correctCount: Int,
        val totalCount: Int
    ) : ReviewUiState
    data class SessionFinished(val correctCount: Int, val totalCount: Int) : ReviewUiState
}

enum class QuestionType {
    MEANING, READING
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val getReviewQueueUseCase: GetReviewQueueUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var originalQueue = listOf<ReviewItem>()
    
    // We separate the active reviews to resolve meanings and readings independently
    private var activeMeaningList = mutableListOf<ReviewItem>()
    private var activeReadingList = mutableListOf<ReviewItem>()
    
    private var totalUniqueItems = 0
    private var completedUniqueCount = 0

    // Track incorrect answers for submission
    private val incorrectMeanings = mutableMapOf<Int, Int>() // SubjectId -> count
    private val incorrectReadings = mutableMapOf<Int, Int>() // SubjectId -> count

    init {
        loadReviewQueue()
    }

    private fun loadReviewQueue() {
        viewModelScope.launch {
            getReviewQueueUseCase().collect { items ->
                if (_uiState.value !is ReviewUiState.Loading) return@collect
                
                if (items.isEmpty()) {
                    _uiState.value = ReviewUiState.Empty
                } else {
                    originalQueue = items.shuffled()
                    totalUniqueItems = originalQueue.size
                    
                    activeMeaningList.addAll(originalQueue)
                    // Radicals do not have readings, so we only add Kanji and Vocabulary to the reading list
                    activeReadingList.addAll(originalQueue.filter { it.subject.type != "radical" })
                    
                    nextQuestion()
                }
            }
        }
    }

    private fun nextQuestion() {
        if (activeMeaningList.isEmpty() && activeReadingList.isEmpty()) {
            _uiState.value = ReviewUiState.SessionFinished(completedUniqueCount, totalUniqueItems)
            return
        }

        // Randomly pick meaning or reading if both exist, otherwise pick the non-empty one
        val nextType = if (activeMeaningList.isNotEmpty() && activeReadingList.isNotEmpty()) {
            if (Math.random() < 0.5) QuestionType.MEANING else QuestionType.READING
        } else if (activeMeaningList.isNotEmpty()) {
            QuestionType.MEANING
        } else {
            QuestionType.READING
        }

        val item = if (nextType == QuestionType.MEANING) {
            activeMeaningList.first()
        } else {
            activeReadingList.first()
        }

        val completed = totalUniqueItems - (activeMeaningList.map { it.subject.id }.toSet() + activeReadingList.map { it.subject.id }.toSet()).size
        completedUniqueCount = completed

        _uiState.value = ReviewUiState.Active(
            currentItem = item,
            questionType = nextType,
            progress = completed.toFloat() / totalUniqueItems.toFloat(),
            correctCount = completed,
            totalCount = totalUniqueItems
        )
    }

    fun submitAnswer(answer: String): Boolean {
        val currentState = _uiState.value as? ReviewUiState.Active ?: return false
        val subject = currentState.currentItem.subject
        val isMeaning = currentState.questionType == QuestionType.MEANING

        val isCorrect = if (isMeaning) {
            // Check meaning answers (case-insensitive)
            subject.meanings.any { it.trim().equals(answer.trim(), ignoreCase = true) }
        } else {
            // Convert Romaji answers to Hiragana for Kanji/Vocab readings check
            val kanaAnswer = Wanakana.toHiragana(answer.trim())
            subject.readings.any { it.trim() == kanaAnswer }
        }

        if (isCorrect) {
            if (isMeaning) {
                activeMeaningList.removeFirst()
            } else {
                activeReadingList.removeFirst()
            }

            // If the item is fully answered (no more meanings or readings pending for this ID)
            val subjectId = subject.id
            val meaningPending = activeMeaningList.any { it.subject.id == subjectId }
            val readingPending = activeReadingList.any { it.subject.id == subjectId }

            if (!meaningPending && !readingPending) {
                // Post review to API
                val incMeaning = incorrectMeanings[subjectId] ?: 0
                val incReading = incorrectReadings[subjectId] ?: 0
                viewModelScope.launch {
                    submitReviewUseCase(subjectId, incMeaning, incReading)
                }
            }

            nextQuestion()
            return true
        } else {
            // Increment incorrect counts
            val subjectId = subject.id
            if (isMeaning) {
                incorrectMeanings[subjectId] = (incorrectMeanings[subjectId] ?: 0) + 1
                // Send failed item to back of the queue
                val first = activeMeaningList.removeFirst()
                activeMeaningList.add(first)
            } else {
                incorrectReadings[subjectId] = (incorrectReadings[subjectId] ?: 0) + 1
                val first = activeReadingList.removeFirst()
                activeReadingList.add(first)
            }
            nextQuestion()
            return false
        }
    }
}
