package es.pile.features.settings.ui.localAI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.core.domain.models.ocr.OcrModelGroupState
import es.pile.core.domain.providers.DeviceLocaleProvider
import es.pile.core.domain.repositories.OcrModelRepository
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsLocalAIViewModel(
    private val ocrModelRepository: OcrModelRepository,
    private val settingsRepository: SettingsRepository,
    private val deviceLocaleProvider: DeviceLocaleProvider
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsLocalAIState())
    val state: StateFlow<SettingsLocalAIState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val recommendedLanguageCodes = deviceLocaleProvider.getLanguageCodes()
            val recommendedLanguageCodesSet = recommendedLanguageCodes.toSet()

            combine(
                settingsRepository.userSettings,
                ocrModelRepository.ocrModelGroupsState
            ) { userSettings, modelGroupStates ->
                val sortedModelGroupStates = modelGroupStates
                    .sortedWith(
                        compareByDescending<OcrModelGroupState> { item -> 
                            item.group.languageCodes.any { it in recommendedLanguageCodesSet }
                        }.thenBy { it.group.getDisplayName(recommendedLanguageCodes) }
                    )

                SettingsLocalAIState(
                    isAIEnabled = userSettings.isLocalAiEnabled,
                    ocrModelGroupsState = sortedModelGroupStates,
                    recommendedLanguageCodes = recommendedLanguageCodes
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun handleEvent(event: SettingsLocalAIEvent) {
        when (event) {
            SettingsLocalAIEvent.OnBackClicked -> {}
            SettingsLocalAIEvent.OnNavigateToManageModels -> {}

            SettingsLocalAIEvent.OnUseAIClicked -> {
                viewModelScope.launch {
                    val currentState = settingsRepository.userSettings.first().isLocalAiEnabled
                    settingsRepository.updateLocalAi(!currentState)
                }
            }

            is SettingsLocalAIEvent.OnOcrModelGroupClicked -> {
                selectOcrModelGroup(event.modelGroupId)
            }

            is SettingsLocalAIEvent.OnDeleteOcrLanguage -> deleteModel(event.modelGroupId)
        }
    }

    private fun selectOcrModelGroup(modelGroupId: String) {
        viewModelScope.launch {
            val ocrModelGroupState = state.value.ocrModelGroupsState.find { it.group.id == modelGroupId } ?: return@launch
            if (ocrModelGroupState.isDownloaded) {
                ocrModelRepository.toggleModelGroupSelection(modelGroupId)
            } else {
                downloadModel(modelGroupId)
            }
        }
    }

    private fun downloadModel(modelGroupId: String) {
        viewModelScope.launch {
            ocrModelRepository.downloadModel(modelGroupId)
        }
    }

    private fun deleteModel(modelGroupId: String) {
        viewModelScope.launch {
            ocrModelRepository.deleteModel(modelGroupId)
        }
    }
}
