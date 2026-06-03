package com.tildemark.alimango.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tildemark.alimango.domain.model.User
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.repository.SubjectRepository
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.usecase.GetDashboardSummaryUseCase
import com.tildemark.alimango.domain.usecase.GetUserUseCase
import com.tildemark.alimango.domain.usecase.ObserveSyncStatusUseCase
import com.tildemark.alimango.domain.usecase.SyncStatus
import com.tildemark.alimango.domain.usecase.TriggerSyncUseCase
import com.tildemark.alimango.domain.usecase.GetLevelProgressUseCase
import com.tildemark.alimango.domain.usecase.LevelProgress
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getLevelProgressUseCase: GetLevelProgressUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase,
    private val subjectRepository: SubjectRepository,
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    // Simple user info flow
    private val _userFlow = kotlinx.coroutines.flow.MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _userFlow

    // Selected subject for detail dialog
    private val _selectedSubject = kotlinx.coroutines.flow.MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject

    init {
        viewModelScope.launch {
            val loadedUser = getUserUseCase()
            Log.d("DashboardViewModel", "Loaded User: $loadedUser")
            _userFlow.value = loadedUser
        }
    }

    val syncStatus: StateFlow<SyncStatus> = observeSyncStatusUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus.Idle
        )

    val dashboardSummary = getDashboardSummaryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val levelProgress: StateFlow<LevelProgress?> = user
        .flatMapLatest { u ->
            Log.d("DashboardViewModel", "flatMapLatest user updated: $u")
            if (u != null) {
                getLevelProgressUseCase(u.level)
            } else {
                flowOf(null)
            }
        }
        .onEach {
            Log.d("DashboardViewModel", "levelProgress onEach: $it")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Save notes & synonyms locally from dialog
    fun saveNoteAndSynonyms(subjectId: Int, note: String, synonyms: List<String>) {
        viewModelScope.launch {
            assignmentRepository.saveNoteAndSynonyms(subjectId, note, synonyms)
            // Refresh detail state
            val current = _selectedSubject.value
            if (current != null && current.id == subjectId) {
                _selectedSubject.value = null
                _selectedSubject.value = current
            }
        }
    }

    // Observe active assignment details for the dialog
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedAssignment: StateFlow<Assignment?> = _selectedSubject
        .flatMapLatest { subj ->
            if (subj != null) {
                assignmentRepository.observeAllAssignments().map { assignments ->
                    assignments.firstOrNull { it.subjectId == subj.id }
                }
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Observe relationships for the dialog
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedRelationships: StateFlow<List<Subject>> = _selectedSubject
        .flatMapLatest { subj ->
            if (subj != null) {
                subjectRepository.observeAllSubjects().map { subjects ->
                    getRelationships(subj, subjects)
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun getRelationships(subject: Subject, allSubjects: List<Subject>): List<Subject> {
        return when (subject.type) {
            "radical" -> {
                val char = subject.characters
                val radicalName = subject.meanings.firstOrNull() ?: ""
                allSubjects.filter {
                    it.type == "kanji" && (
                        (char != null && it.characters?.contains(char) == true) ||
                        (it.meaningMnemonic.contains(radicalName, ignoreCase = true))
                    )
                }.take(12)
            }
            "kanji" -> {
                val char = subject.characters
                if (char != null) {
                    allSubjects.filter {
                        (it.type == "vocabulary" || it.type == "kana_vocabulary") &&
                        it.characters?.contains(char) == true
                    }.take(12)
                } else {
                    emptyList()
                }
            }
            "vocabulary", "kana_vocabulary" -> {
                val vocabChars = subject.characters ?: ""
                allSubjects.filter {
                    it.type == "kanji" &&
                    it.characters != null &&
                    vocabChars.contains(it.characters)
                }.take(12)
            }
            else -> emptyList()
        }
    }

    fun showSubjectDetail(subject: Subject?) {
        _selectedSubject.value = subject
    }

    fun triggerSync() {
        triggerSyncUseCase()
    }
}
