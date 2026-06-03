package com.tildemark.alimango.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsBrowserViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Active menu: "levels", "radical", "kanji", "vocabulary"
    private val _selectedMenu = MutableStateFlow("levels")
    val selectedMenu: StateFlow<String> = _selectedMenu

    private val _selectedLevel = MutableStateFlow<Int?>(1)
    val selectedLevel: StateFlow<Int?> = _selectedLevel

    // Selected subject for showing detail dialog
    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject

    // Save notes & synonyms locally
    fun saveNoteAndSynonyms(subjectId: Int, note: String, synonyms: List<String>) {
        viewModelScope.launch {
            assignmentRepository.saveNoteAndSynonyms(subjectId, note, synonyms)
            // Refresh selected subject to update UI if currently showing
            val current = _selectedSubject.value
            if (current != null && current.id == subjectId) {
                _selectedSubject.value = null
                _selectedSubject.value = current
            }
        }
    }

    // Load assignment for the selected subject
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

    // Load relationships (Found in Kanji / Found in Vocabulary / Kanji composition)
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

    // Get list of unique levels present in the database to show in the selector
    val availableLevels: StateFlow<List<Int>> = subjectRepository.observeAllSubjects()
        .combine(_selectedMenu) { allSubjects, menu ->
            allSubjects
                .map { it.level }
                .distinct()
                .sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(1)
        )

    val subjects: StateFlow<List<Subject>> = combine(
        subjectRepository.observeAllSubjects(),
        _searchQuery,
        _selectedMenu,
        _selectedLevel
    ) { allSubjects, query, menu, level ->
        var filtered = allSubjects

        // Filter based on selected Menu tab
        when (menu) {
            "levels" -> {
                if (level != null) {
                    filtered = filtered.filter { it.level == level }
                }
            }
            "radical" -> {
                filtered = filtered.filter { it.type == "radical" }
            }
            "kanji" -> {
                filtered = filtered.filter { it.type == "kanji" }
            }
            "vocabulary" -> {
                filtered = filtered.filter { it.type == "vocabulary" || it.type == "kana_vocabulary" }
            }
        }

        // Apply Search query
        if (query.isNotBlank()) {
            filtered = filtered.filter { subject ->
                subject.characters?.contains(query, ignoreCase = true) == true ||
                subject.meanings.any { it.contains(query, ignoreCase = true) } ||
                subject.readings.any { it.contains(query, ignoreCase = true) }
            }
        }

        filtered.sortedWith(compareBy<Subject> { it.level }.thenBy { it.id })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onMenuSelected(menu: String) {
        _selectedMenu.value = menu
    }

    fun onLevelSelected(level: Int?) {
        _selectedLevel.value = level
    }

    fun showSubjectDetail(subject: Subject?) {
        _selectedSubject.value = subject
    }
}
