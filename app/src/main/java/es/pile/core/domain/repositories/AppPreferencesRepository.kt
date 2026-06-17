package es.pile.core.domain.repositories

import es.pile.core.domain.models.AppPreferences
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val appPreferences: Flow<AppPreferences>
    suspend fun updateOnboardingCompleted(completed: Boolean)
}