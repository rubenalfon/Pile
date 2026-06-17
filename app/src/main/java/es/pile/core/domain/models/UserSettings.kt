package es.pile.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val isMaterialColor: Boolean = true,
    val isLocalAiEnabled: Boolean = false,
    val selectedModel: String? = null,
    val imageResolution: ImageResolution = ImageResolution.ORIGINAL
)