package com.tildemark.alimango.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tildemark.alimango.domain.usecase.GetUserUseCase
import com.tildemark.alimango.domain.usecase.TriggerSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingUiState {
    object Idle : OnboardingUiState
    object Loading : OnboardingUiState
    object Success : OnboardingUiState
    data class Error(val message: String) : OnboardingUiState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun submitPat(pat: String) {
        if (pat.isBlank()) {
            _uiState.value = OnboardingUiState.Error("PAT cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            val user = getUserUseCase(pat)
            if (user != null) {
                // Successfully validated & saved.
                // Trigger initial synchronization.
                triggerSyncUseCase()
                _uiState.value = OnboardingUiState.Success
            } else {
                _uiState.value = OnboardingUiState.Error("Invalid Personal Access Token")
            }
        }
    }
}
