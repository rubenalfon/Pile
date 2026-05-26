package com.ganadoro.pile.features.settings.ui.resolution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.core.domain.models.ImageResolution
import com.ganadoro.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsResolutionViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val state: StateFlow<SettingsResolutionState> = settingsRepository.userSettings
        .map { userSettings ->
            SettingsResolutionState(
                isLoading = false,
                imageResolution = userSettings.imageResolution
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsResolutionState()
        )

    fun handleEvent(event: SettingsResolutionEvent) {
        when (event) {
            SettingsResolutionEvent.OnBackClicked -> {}
            is SettingsResolutionEvent.OnResolutionChanged -> {
                updateResolution(event.newResolution)
            }
        }
    }

    private fun updateResolution(newResolution: ImageResolution) {
        viewModelScope.launch {
            settingsRepository.updateImageResolution(newResolution)
        }
    }
}