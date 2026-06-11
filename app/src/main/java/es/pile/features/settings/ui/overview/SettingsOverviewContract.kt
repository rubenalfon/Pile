package es.pile.features.settings.ui.overview

import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.models.ImageResolution

data class SettingsOverviewState(
    val isLoading: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val isMaterialColor: Boolean = true,
    val isLocalAiEnabled: Boolean = false,
    val selectedModel: String? = null,
    val imageResolution: ImageResolution = ImageResolution.LOW,
)

sealed interface SettingsOverviewEvent {
    data object OnBackClicked : SettingsOverviewEvent
    data object OnResolutionClicked : SettingsOverviewEvent
    data class OnThemeChanged(val newTheme: AppTheme) : SettingsOverviewEvent
    data object OnMaterialColorToggled : SettingsOverviewEvent
    data object OnLocalAiToggled : SettingsOverviewEvent
}