package es.pile.core.data.repositories

import androidx.datastore.core.DataStore
import es.pile.core.domain.models.AppPreferences
import es.pile.core.domain.repositories.AppPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DataStoreAppPreferencesRepository(
    private val dataStore: DataStore<AppPreferences>,
    private val ioDispatcher: CoroutineDispatcher
) : AppPreferencesRepository {
    override val appPreferences: Flow<AppPreferences> = dataStore.data

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(isOnboardingCompleted = completed) }
        }
    }
}