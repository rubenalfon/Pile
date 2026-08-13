package es.pile.core.data.sync

import android.content.Context
import android.net.ConnectivityManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import es.pile.core.data.backup.BackupException
import es.pile.core.data.backup.EncryptionKeyRequiredException
import es.pile.core.data.backup.InvalidEncryptionKeyException
import es.pile.core.domain.models.SyncState
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.sync.SyncManager
import es.pile.core.domain.usecases.sync.PerformSyncUseCase
import es.pile.core.ui.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class SyncManagerImpl(
    private val context: Context,
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val settingsRepository: SettingsRepository,
    private val performSyncUseCase: PerformSyncUseCase,
    private val externalScope: CoroutineScope
) : SyncManager {

    private val workManager = WorkManager.getInstance(context)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var autoSyncStarted = false

    init {
        // Observe WorkManager to update syncState
        workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.WORK_NAME)
            .onEach { workInfos ->
                val info = workInfos.firstOrNull() ?: return@onEach
                val settings = settingsRepository.userSettings.first()
                
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        val stateStr = info.progress.getString(SyncWorker.PROGRESS_STATE_KEY)
                        _syncState.value = when (stateStr) {
                            SyncWorker.STATE_DOWNLOADING -> SyncState.Downloading
                            SyncWorker.STATE_UPLOADING -> SyncState.Uploading
                            else -> SyncState.Syncing
                        }
                    }

                    WorkInfo.State.ENQUEUED -> {
                        if (!settings.isBackupOverCellularEnabled && isOnMeteredConnection()) {
                            _syncState.value = SyncState.WaitingForWifi
                        } else if (_syncState.value == SyncState.WaitingForWifi) {
                             _syncState.value = SyncState.Idle
                        }
                    }

                    WorkInfo.State.SUCCEEDED -> checkProviderAndSetIdle()

                    WorkInfo.State.FAILED -> {
                        val errorType = info.outputData.getString(SyncWorker.ERROR_TYPE_KEY)
                        val errorMessage = info.outputData.getString(SyncWorker.ERROR_MESSAGE_KEY) ?: "Sync failed"
                        
                        _syncState.value = when (errorType) {
                            SyncWorker.ERROR_TYPE_INVALID_KEY -> SyncState.InvalidKey
                            SyncWorker.ERROR_TYPE_KEY_REQUIRED -> SyncState.KeyRequired
                            else -> SyncState.Error(UiText.DynamicString(errorMessage))
                        }
                    }
                    else -> {
                        if (_syncState.value == SyncState.WaitingForWifi) {
                             _syncState.value = SyncState.Idle
                        }
                    }
                }
            }.launchIn(externalScope)

        settingsRepository.userSettings
            .map { it.selectedBackupProviderName to it.isBackupOverCellularEnabled }
            .distinctUntilChanged()
            .onEach { (providerName, _) ->
                if (providerName == null) {
                    _syncState.value = SyncState.NoProvider
                } else {
                    if (_syncState.value == SyncState.NoProvider) {
                        _syncState.value = SyncState.Idle
                    }
                    requestSync(force = false)
                }
            }.launchIn(externalScope)
    }

    private fun checkProviderAndSetIdle() {
        externalScope.launch {
            val settings = settingsRepository.userSettings.first()
            if (settings.selectedBackupProviderName == null) {
                _syncState.value = SyncState.NoProvider
            } else {
                val now = System.currentTimeMillis()
                settingsRepository.updateLastSyncTimestamp(now)
                _syncState.value = SyncState.Success(now) // TODO: Se guarda el tiempo ahora? no tiene sentido
            }
        }
    }

    override fun requestSync(force: Boolean) {
        externalScope.launch {
            val settings = settingsRepository.userSettings.first()
            if (settings.selectedBackupProviderName == null) {
                _syncState.value = SyncState.NoProvider
                return@launch
            }

            val networkType = if (settings.isBackupOverCellularEnabled) {
                NetworkType.CONNECTED
            } else {
                NetworkType.UNMETERED
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniqueWork(
                SyncWorker.WORK_NAME,
                if (force) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
                syncRequest
            )
        }
    }

    override fun startAutoSync() {
        if (autoSyncStarted) return
        autoSyncStarted = true

        // Observe local changes with debounce
        combine(
            documentModelRepository.documentModels,
            pileModelRepository.pileModels
        ) { docs, piles ->
            docs.size + piles.size
        }
            .distinctUntilChanged()
            .drop(1)
            .debounce(15.seconds)
            .onEach {
                requestSync(force = true)
            }
            .launchIn(externalScope)
            
        // Initial sync on start
        requestSync(force = false)
    }

    override suspend fun validateAndSetKey(key: String): Result<Unit> {
        _syncState.value = SyncState.VerifyingKey

        return performSyncUseCase(tempMasterKey = key).fold(
            onSuccess = {
                settingsRepository.saveBackupMasterKey(key)
                settingsRepository.updateBackupEncryption(true)
                requestSync(force = true)
                Result.success(Unit)
            },
            onFailure = { error ->
                val syncError = when (error) {
                    is InvalidEncryptionKeyException -> SyncState.InvalidKey
                    is EncryptionKeyRequiredException -> SyncState.KeyRequired
                    is BackupException -> SyncState.Error(error.uiText)
                    else -> SyncState.Error(UiText.DynamicString(error.message ?: "Validation failed"))
                }
                _syncState.value = syncError
                Result.failure(error)
            }
        )
    }

    private fun isOnMeteredConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.isActiveNetworkMetered
    }
}
