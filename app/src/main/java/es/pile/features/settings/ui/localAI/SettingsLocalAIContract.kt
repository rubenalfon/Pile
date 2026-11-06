package es.pile.features.settings.ui.localAI

import es.pile.core.domain.models.ocr.OcrModelGroupState

data class SettingsLocalAIState(
    val isAIEnabled : Boolean = false,

    val ocrModelGroupsState: List<OcrModelGroupState> = emptyList(),
    val recommendedLanguageCodes: List<String> = emptyList(),

//    val activeDownloads: Map<String, es.pile.core.domain.models.DownloadState> = emptyMap(),
//    val llmModels: List<> = emptyList(),
    )

sealed interface SettingsLocalAIEvent {
    data object OnBackClicked : SettingsLocalAIEvent
    data object OnNavigateToManageModels : SettingsLocalAIEvent

    data object OnUseAIClicked : SettingsLocalAIEvent

    data class OnOcrModelGroupClicked(val modelGroupId: String) : SettingsLocalAIEvent
//    data class OnLlmModelClicked(val * : String) : SettingsLocalAIEvent

    data class OnDeleteOcrLanguage(val modelGroupId: String) : SettingsLocalAIEvent
//    data class OnDeleteLlmModel(val * : ) : SettingsLocalAIEvent
}