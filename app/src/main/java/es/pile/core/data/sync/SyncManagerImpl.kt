package es.pile.core.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import es.pile.core.domain.models.SyncState
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.sync.SyncManager
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
                when (info.state) {
                    WorkInfo.State.RUNNING -> {
                        val stateStr = info.progress.getString("STATE")
                        _syncState.value = when (stateStr) {
                            "DOWNLOADING" -> SyncState.Downloading
                            "UPLOADING" -> SyncState.Uploading
                            else -> SyncState.Syncing
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> checkProviderAndSetIdle()
                    WorkInfo.State.FAILED -> _syncState.value = SyncState.Error(UiText.DynamicString("Sync failed"))
                    else -> {}
                }
            }.launchIn(externalScope)

        // Observe settings for provider changes
        settingsRepository.userSettings
            .map { it.selectedBackupProviderName }
            .distinctUntilChanged()
            .onEach { providerName ->
                if (providerName == null) {
                    _syncState.value = SyncState.NoProvider
                } else if (_syncState.value == SyncState.NoProvider) {
                    _syncState.value = SyncState.Idle
                }
            }.launchIn(externalScope)
    }

    private fun checkProviderAndSetIdle() {
        externalScope.launch {
            val settings = settingsRepository.userSettings.first()
            _syncState.value = if (settings.selectedBackupProviderName == null) {
                SyncState.NoProvider
            } else {
                SyncState.Success(System.currentTimeMillis())
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

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
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
}

