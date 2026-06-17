package es.pile.core.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AppPreferences(
    val isOnboardingCompleted: Boolean = false
)