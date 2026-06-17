package es.pile.features.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.core.domain.repositories.AppPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun handleEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnNextClicked -> {
                _state.update { it.copy(currentPage = it.currentPage + 1) }
            }
            OnboardingEvent.OnBackClicked -> {
                _state.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
            }
            OnboardingEvent.OnFinished -> {
                viewModelScope.launch {
                    appPreferencesRepository.updateOnboardingCompleted(true)
                }
            }
        }
    }
}