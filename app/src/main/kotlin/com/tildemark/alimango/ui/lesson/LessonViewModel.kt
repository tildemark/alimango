package com.tildemark.alimango.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tildemark.alimango.domain.usecase.GetLessonQueueUseCase
import com.tildemark.alimango.domain.usecase.LessonItem
import com.tildemark.alimango.domain.usecase.StartAssignmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.esnault.wanakana.core.Wanakana
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle

sealed interface LessonUiState {
    object Loading : LessonUiState
    object Empty : LessonUiState
    
    data class Slides(
        val items: List<LessonItem>,
        val currentItemIndex: Int,
        val currentSlideIndex: Int
    ) : LessonUiState

    data class Quiz(
        val items: List<LessonItem>,
        val questions: List<QuizQuestion>,
        val currentQuestionIndex: Int,
        val inputAnswer: String = "",
        val showError: Boolean = false
    ) : LessonUiState

    data class Summary(
        val itemsCount: Int
    ) : LessonUiState
}

data class QuizQuestion(
    val item: LessonItem,
    val isMeaning: Boolean
)

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonQueueUseCase: GetLessonQueueUseCase,
    private val startAssignmentUseCase: StartAssignmentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customSubjectIds: List<Int>? = savedStateHandle.get<String>("subjectIds")
        ?.split(",")
        ?.mapNotNull { it.toIntOrNull() }

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        loadLessons()
    }

    fun loadLessons() {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading
            val lessons = if (!customSubjectIds.isNullOrEmpty()) {
                getLessonQueueUseCase(limit = 9999, subjectIds = customSubjectIds)
            } else {
                getLessonQueueUseCase()
            }
            lessons.forEach { item ->
                android.util.Log.d("LessonDB", "Subject ID: ${item.subject.id}, type: ${item.subject.type}, characters: ${item.subject.characters}, meanings: ${item.subject.meanings}, readings: ${item.subject.readings}")
            }
            if (lessons.isEmpty()) {
                _uiState.value = LessonUiState.Empty
            } else {
                _uiState.value = LessonUiState.Slides(
                    items = lessons,
                    currentItemIndex = 0,
                    currentSlideIndex = 0
                )
            }
        }
    }

    fun nextSlide() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Slides) {
            val currentItem = currentState.items[currentState.currentItemIndex]
            val maxSlides = if (currentItem.subject.readings.isEmpty()) 2 else 3

            if (currentState.currentSlideIndex < maxSlides - 1) {
                // Move to next slide for current item
                _uiState.update {
                    (it as LessonUiState.Slides).copy(currentSlideIndex = currentState.currentSlideIndex + 1)
                }
            } else if (currentState.currentItemIndex < currentState.items.size - 1) {
                // Move to first slide of next item
                _uiState.update {
                    (it as LessonUiState.Slides).copy(
                        currentItemIndex = currentState.currentItemIndex + 1,
                        currentSlideIndex = 0
                    )
                }
            } else {
                // Transition to Quiz Mode!
                startQuiz(currentState.items)
            }
        }
    }

    fun prevSlide() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Slides) {
            if (currentState.currentSlideIndex > 0) {
                _uiState.update {
                    (it as LessonUiState.Slides).copy(currentSlideIndex = currentState.currentSlideIndex - 1)
                }
            } else if (currentState.currentItemIndex > 0) {
                val prevItemIndex = currentState.currentItemIndex - 1
                val prevItem = currentState.items[prevItemIndex]
                val prevMaxSlides = if (prevItem.subject.readings.isEmpty()) 2 else 3
                _uiState.update {
                    (it as LessonUiState.Slides).copy(
                        currentItemIndex = prevItemIndex,
                        currentSlideIndex = prevMaxSlides - 1
                    )
                }
            }
        }
    }

    private fun startQuiz(items: List<LessonItem>) {
        val questions = mutableListOf<QuizQuestion>()
        items.forEach { item ->
            // Meaning question
            questions.add(QuizQuestion(item, isMeaning = true))
            // Reading question if readings are available
            if (item.subject.readings.isNotEmpty()) {
                questions.add(QuizQuestion(item, isMeaning = false))
            }
        }
        // Shuffle questions to make it interactive and challenging
        questions.shuffle()

        _uiState.value = LessonUiState.Quiz(
            items = items,
            questions = questions,
            currentQuestionIndex = 0
        )
    }

    fun onAnswerChanged(answer: String, isMeaning: Boolean) {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Quiz) {
            // Apply romaji-to-kana conversion using Wanakana if it's reading
            val converted = if (!isMeaning) {
                Wanakana.toHiragana(answer)
            } else {
                answer
            }
            _uiState.update {
                (it as LessonUiState.Quiz).copy(inputAnswer = converted, showError = false)
            }
        }
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        if (currentState is LessonUiState.Quiz) {
            val currentQuestion = currentState.questions[currentState.currentQuestionIndex]
            val answer = currentState.inputAnswer.trim()
            val subject = currentQuestion.item.subject

            val isCorrect = if (currentQuestion.isMeaning) {
                subject.meanings.any { it.trim().equals(answer, ignoreCase = true) }
            } else {
                val hiraganaAnswer = Wanakana.toHiragana(answer).trim()
                val katakanaAnswer = Wanakana.toKatakana(answer).trim()
                subject.readings.any { 
                    val r = it.trim()
                    r == hiraganaAnswer || r == katakanaAnswer || Wanakana.toHiragana(r) == hiraganaAnswer
                }
            }

            if (isCorrect) {
                // Correct answer!
                val nextIndex = currentState.currentQuestionIndex + 1
                if (nextIndex < currentState.questions.size) {
                    _uiState.update {
                        (it as LessonUiState.Quiz).copy(
                            currentQuestionIndex = nextIndex,
                            inputAnswer = "",
                            showError = false
                        )
                    }
                } else {
                    // Quiz finished! Start all assignments in the batch
                    startAllAssignments(currentState.items)
                }
            } else {
                // Incorrect answer
                _uiState.update {
                    (it as LessonUiState.Quiz).copy(showError = true)
                }
            }
        }
    }

    private fun startAllAssignments(items: List<LessonItem>) {
        _uiState.value = LessonUiState.Loading
        viewModelScope.launch {
            items.forEach { item ->
                startAssignmentUseCase(item.assignment.id)
            }
            _uiState.value = LessonUiState.Summary(items.size)
        }
    }
}
