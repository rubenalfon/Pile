package es.pile.features.settings.ui.overview

import android.net.Uri
import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.models.ImageResolution
import es.pile.core.ui.util.UiText

data class SettingsOverviewState(
    val isLoading: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val isMaterialColor: Boolean = true,
    val isLocalAiEnabled: Boolean = false,
    val selectedModel: String? = null,
    val imageResolution: ImageResolution = ImageResolution.LOW,
    val isBackupSupported: Boolean = false,
    val snackbarMessage: UiText? = null
)

sealed interface SettingsOverviewEvent {
    data object OnBackClicked : SettingsOverviewEvent

    data object OnResolutionClicked : SettingsOverviewEvent
    data class OnThemeChanged(val newTheme: AppTheme) : SettingsOverviewEvent
    data object OnMaterialColorToggled : SettingsOverviewEvent

    data object OnLocalAiToggled : SettingsOverviewEvent

    data object OnBackupClicked : SettingsOverviewEvent
    
    data class OnExportUriSelected(val uri: Uri) : SettingsOverviewEvent
    data class OnImportUriSelected(val uri: Uri) : SettingsOverviewEvent
    data object OnSnackbarMessageShown : SettingsOverviewEvent
}