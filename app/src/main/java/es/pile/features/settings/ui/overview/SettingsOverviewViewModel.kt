package es.pile.features.settings.ui.overview

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.R
import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.usecases.backup.ExportLocalBackupUseCase
import es.pile.core.domain.usecases.backup.ImportLocalBackupUseCase
import es.pile.core.ui.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsOverviewViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val exportLocalBackupUseCase: ExportLocalBackupUseCase,
    private val importLocalBackupUseCase: ImportLocalBackupUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsOverviewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userSettings.collect { userSettings ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        theme = userSettings.theme,
                        isMaterialColor = userSettings.isMaterialColor,
                        isLocalAiEnabled = userSettings.isLocalAiEnabled,
                        selectedModel = userSettings.selectedModel,
                        imageResolution = userSettings.imageResolution,
                        isBackupSupported = backupRepository.availableProviders.isNotEmpty()
                    )
                }
            }
        }
    }

    fun handleEvent(event: SettingsOverviewEvent) {
        when (event) {
            SettingsOverviewEvent.OnBackClicked -> {}
            SettingsOverviewEvent.OnResolutionClicked -> {}
            SettingsOverviewEvent.OnBackupClicked -> {}

            is SettingsOverviewEvent.OnThemeChanged -> updateTheme(event.newTheme)
            SettingsOverviewEvent.OnMaterialColorToggled -> updateMaterialColor()
            SettingsOverviewEvent.OnLocalAiToggled -> updateLocalAi()

            is SettingsOverviewEvent.OnExportUriSelected -> {
                exportData(event.uri)
            }

            is SettingsOverviewEvent.OnImportUriSelected -> {
                importData(event.uri)
            }

            SettingsOverviewEvent.OnSnackbarMessageShown -> {
                _state.update { it.copy(snackbarMessage = null) }
            }
        }
    }

    private fun exportData(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            exportLocalBackupUseCase(uri).fold(
                onSuccess = {
                    _state.update { it.copy(snackbarMessage = UiText.StringResource(R.string.export_success)) }
                },
                onFailure = {
                    _state.update { it.copy(snackbarMessage = UiText.StringResource(R.string.export_error)) }
                }
            )
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun importData(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            importLocalBackupUseCase(uri).fold(
                onSuccess = {
                    _state.update { it.copy(snackbarMessage = UiText.StringResource(R.string.import_success)) }
                },
                onFailure = {
                    _state.update { it.copy(snackbarMessage = UiText.StringResource(R.string.import_error)) }
                }
            )
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun updateTheme(newTheme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.updateTheme(newTheme)
        }
    }

    private fun updateMaterialColor() {
        viewModelScope.launch {
            settingsRepository.updateMaterialColor(!state.value.isMaterialColor)
        }
    }

    private fun updateLocalAi() {
        viewModelScope.launch {
            settingsRepository.updateLocalAi(!state.value.isLocalAiEnabled)
        }
    }
}