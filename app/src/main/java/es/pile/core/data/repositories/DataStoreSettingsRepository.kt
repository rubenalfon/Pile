package es.pile.core.data.repositories

import androidx.datastore.core.DataStore
import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.models.ImageResolution
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.SecureStorageRepository
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class DataStoreSettingsRepository(
    private val dataStore: DataStore<UserSettings>,
    private val secureStorageRepository: SecureStorageRepository,
    private val ioDispatcher: CoroutineDispatcher
) : SettingsRepository {

    companion object {
        private const val BACKUP_MASTER_KEY = "backup_master_key"
    }

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

    override suspend fun updateSelectedBackupProvider(name: String?) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(selectedBackupProviderName = name) }
        }
    }

    override suspend fun updateBackupOverCellular(enable: Boolean) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(isBackupOverCellularEnabled = enable) }
        }
    }

    override suspend fun updateBackupEncryption(enable: Boolean) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(isBackupEncryptionEnabled = enable) }
        }
    }

    override suspend fun getBackupMasterKey(): String? {
        return secureStorageRepository.getSecret(BACKUP_MASTER_KEY)
    }

    override suspend fun saveBackupMasterKey(key: String) {
        secureStorageRepository.saveSecret(BACKUP_MASTER_KEY, key)
    }

    override suspend fun removeBackupMasterKey() {
        secureStorageRepository.removeSecret(BACKUP_MASTER_KEY)
    }

    override suspend fun updateLastSyncTimestamp(timestamp: Long?) {
        withContext(ioDispatcher) {
            dataStore.updateData { it.copy(lastSyncTimestamp = timestamp) }
        }
    }
}

