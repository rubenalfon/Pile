package es.pile.features.settings.ui.resolution

import es.pile.core.domain.models.ImageResolution


data class SettingsResolutionState(
    val isLoading: Boolean = true,
    val imageResolution: ImageResolution = ImageResolution.LOW
)

sealed interface SettingsResolutionEvent {
    data object OnBackClicked : SettingsResolutionEvent
    data class OnResolutionChanged(val newResolution: ImageResolution) : SettingsResolutionEvent
}