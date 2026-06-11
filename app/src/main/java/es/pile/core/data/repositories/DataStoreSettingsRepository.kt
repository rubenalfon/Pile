package es.pile.core.data.repositories

import androidx.datastore.core.DataStore
import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.models.ImageResolution
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class DataStoreSettingsRepository(
    private val dataStore: DataStore<UserSettings>,
    private val ioDispatcher: CoroutineDispatcher
) : SettingsRepository {
    override val userSettings: Flow<UserSettings> = dataStore.data

    override suspend fun updateUserSettings(userSettings: UserSettings) {
        withContext(ioDispatcher) {
            dataStore.updateData { userSettings }
        }
    }

    override suspend fun updateTheme(theme: AppTheme) {
        withContext(ioDispatcher) {
            dataStore.updateData {
                it.copy(theme = theme)
            }
        }
    }

    override suspend fun updateMaterialColor(enable: Boolean) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(isMaterialColor = enable) }
        }
    }

    override suspend fun updateLocalAi(enable: Boolean) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(isLocalAiEnabled = enable) }
        }
    }

    override suspend fun updateSelectedModel(model: String?) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(selectedModel = model) }
        }
    }

    override suspend fun updateImageResolution(resolution: ImageResolution) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(imageResolution = resolution) }
        }
    }
}
