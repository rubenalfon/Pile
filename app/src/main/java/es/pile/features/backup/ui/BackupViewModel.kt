package es.pile.features.backup.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.R
import es.pile.core.data.backup.EncryptionKeyRequiredException
import es.pile.core.data.backup.InvalidEncryptionKeyException
import es.pile.core.domain.backup.BackupProvider
import es.pile.core.domain.backup.BackupProviderInfo
import es.pile.core.domain.backup.UserCancelledException
import es.pile.core.domain.backup.toInfo
import es.pile.core.domain.models.SyncState
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.sync.SyncManager
import es.pile.core.ui.util.UiText
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class BackupViewModel(
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state =
        MutableStateFlow(BackupState(availableProviders = backupRepository.availableProviders.map { it.toInfo() }))
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private var authJob: Job? = null
    private var resolutionDeferred: CompletableDeferred<Result<Intent>>? = null

    init {
        settingsRepository.userSettings.onEach { settings ->
            val provider = backupRepository.availableProviders.find {
                it.name == settings.selectedBackupProviderName
            }

            val oldSelectedProviderName = state.value.selectedProvider?.name

            _state.update {
                it.copy(
                    selectedProvider = provider?.toInfo(),
                    backupUsingCellular = settings.isBackupOverCellularEnabled,
                    isEncryptionOn = settings.isBackupEncryptionEnabled
                )
            }

            val isFirstLoad = provider != null && oldSelectedProviderName == null
            val hasProviderChanged = provider != null && oldSelectedProviderName != provider.name
            val isAlreadyAuthenticating = authJob?.isActive == true

            if ((isFirstLoad || hasProviderChanged) && !isAlreadyAuthenticating) {
                authenticateAndSelectProvider(provider, isInitialRestore = true)
            } else if (provider == null) {
                _state.update { it.copy(isLoading = false) }
            } else if (!isAlreadyAuthenticating) {
                _state.update { it.copy(isLoading = false) }
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            syncManager.syncState.collect { syncState ->
                _state.update { it.copy(syncState = syncState) }

                when (syncState) {
                    is SyncState.Success -> {
                        getSelectedProvider()?.let { loadStatus(it, updateLoading = false) }
                    }
                    SyncState.InvalidKey -> {
                        _state.update {
                            it.copy(
                                isEnterKeyDialogVisible = true,
                                enterKeyError = UiText.StringResource(R.string.invalid_recovery_key)
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private val onResolutionRequired: suspend (android.app.PendingIntent) -> Result<Intent> =
        { pendingIntent ->
            resolutionDeferred = CompletableDeferred()
            _state.update { it.copy(pendingResolution = pendingIntent) }
            resolutionDeferred!!.await()
        }

    fun handleEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.OnBackClicked -> {}
            BackupEvent.OnNavigateToEncryption -> {}

            is BackupEvent.OnProviderSelected -> selectProvider(event.provider)
            BackupEvent.OnCellularBackupToggled -> toggleCellularBackup()
            is BackupEvent.OnResolutionResult -> handleResolutionResult(event.result)
            BackupEvent.OnRetryAuthentication -> {
                _state.update { it.copy(isAuthErrorAlertVisible = false) }
                getSelectedProvider()?.let {
                    authenticateAndSelectProvider(
                        it,
                        isInitialRestore = false
                    )
                }
            }

            BackupEvent.OnCancelAuthentication -> {
                _state.update { it.copy(isAuthErrorAlertVisible = false) }
                disableBackup()
            }

            BackupEvent.OnSwitchAccountClicked -> {
                _state.update { it.copy(isAccountPickerVisible = true) }
            }

            is BackupEvent.OnAccountSelected -> {
                _state.update { it.copy(isAccountPickerVisible = false) }
                val email = event.email
                if (email != null) {
                    getSelectedProvider()?.let {
                        authenticateAndSelectProvider(
                            it,
                            isInitialRestore = false,
                            selectedAccountEmail = email
                        )
                    }
                }
            }

            BackupEvent.OnManageStorageClicked -> {
                _state.update { it.copy(navigateToUrl = UiText.StringResource(R.string.google_manage_storage_url)) }
            }

            BackupEvent.OnUrlNavigated -> {
                _state.update { it.copy(navigateToUrl = null) }
            }

            BackupEvent.OnSyncClicked -> syncManager.requestSync(force = true)

            is BackupEvent.OnEnterKeySubmitted -> {
                _state.update { it.copy(enterKeyError = null) }
                syncWithKey(event.key)
            }

            BackupEvent.OnDismissEnterKeyDialog -> {
                _state.update { it.copy(isEnterKeyDialogVisible = false, enterKeyError = null) }
            }
        }
    }

    private fun getSelectedProvider(): BackupProvider? {
        val selectedName = state.value.selectedProvider?.name ?: return null
        return backupRepository.availableProviders.find { it.name == selectedName }
    }

    private fun selectProvider(providerInfo: BackupProviderInfo?) {
        if (providerInfo == null) {
            if (state.value.selectedProvider != null) {
                disableBackup()
            }
            return
        }

        val provider = backupRepository.availableProviders.find { it.name == providerInfo.name }
        if (provider != null) {
            authenticateAndSelectProvider(provider, isInitialRestore = false)
        }
    }

    private fun authenticateAndSelectProvider(
        provider: BackupProvider,
        isInitialRestore: Boolean,
        selectedAccountEmail: String? = null
    ) {
        authJob?.cancel()

        authJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    syncState = SyncState.Idle,
                    isAuthErrorAlertVisible = false
                )
            }

            provider.authenticate(
                onResolutionRequired = onResolutionRequired,
                selectedAccountEmail = selectedAccountEmail
            ).onSuccess {
                if (!isInitialRestore) {
                    settingsRepository.updateSelectedBackupProvider(provider.name)
                }

                // Coordination: load all data before showing the UI
                val statusResult = backupRepository.getSyncStatus(provider)
                val accountResult = provider.getAccountInfo()
                val storageResult = provider.getStorageInfo()

                _state.update {
                    it.copy(
                        selectedProvider = provider.toInfo(),
                        syncStatus = statusResult.getOrNull(),
                        accountEmail = accountResult.getOrNull()?.email,
                        storageUsage = storageResult.getOrNull()?.let { storage ->
                            val totalStr = formatSize(storage.totalBytes)
                            val usedStr = formatSize(storage.usedBytes)
                            val appUsedStr = formatSize(storage.appUsedBytes)
                            UiText.StringResource(
                                R.string.storage_usage_format,
                                usedStr,
                                totalStr,
                                appUsedStr
                            )
                        },
                        isLoading = false,
                        isAuthErrorAlertVisible = false,
                        pendingResolution = null
                    )
                }
            }.onFailure { e ->
                if (e is UserCancelledException) {
                    _state.update { it.copy(isLoading = false) }
                    return@onFailure
                }

                Napier.e { "Authentication failed: ${e.message}" }
                _state.update {
                    it.copy(
                        isLoading = false,
                        isAuthErrorAlertVisible = true,
                        syncState = SyncState.Error(
                            UiText.StringResource(
                                R.string.login_error_format,
                                e.message ?: ""
                            )
                        ),
                        selectedProvider = provider.toInfo()
                    )
                }
            }
        }
    }

    private fun handleResolutionResult(result: Result<Intent>) {
        _state.update { it.copy(pendingResolution = null) }
        resolutionDeferred?.complete(result)
    }

    private fun disableBackup() {
        viewModelScope.launch {
            settingsRepository.updateSelectedBackupProvider(null)
        }
        _state.update {
            it.copy(
                selectedProvider = null,
                accountEmail = null,
                storageUsage = null,
                syncStatus = null,
                isLoading = false
            )
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun syncWithKey(tempKey: String) {
        val provider = getSelectedProvider() ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    syncState = SyncState.Syncing,
                    isCheckingKey = true
                )
            }

            provider.authenticate(
                onResolutionRequired = onResolutionRequired
            ).onSuccess {
                backupRepository.sync(provider, tempKey)
                    .onSuccess {
                        settingsRepository.saveBackupMasterKey(tempKey)
                        settingsRepository.updateBackupEncryption(true)
                        _state.update {
                            it.copy(
                                isEnterKeyDialogVisible = false,
                                isCheckingKey = false,
                                syncState = SyncState.Success(System.currentTimeMillis()),
                                pendingResolution = null
                            )
                        }
                        syncManager.requestSync(force = true)
                        loadStatus(provider, updateLoading = false)
                    }
                    .onFailure { e ->
                        when (e) {
                            is EncryptionKeyRequiredException -> {
                                _state.update {
                                    it.copy(
                                        syncState = SyncState.Idle,
                                        isCheckingKey = false,
                                        isEnterKeyDialogVisible = true
                                    )
                                }
                            }

                            is InvalidEncryptionKeyException -> {
                                _state.update {
                                    it.copy(
                                        syncState = SyncState.Idle,
                                        isCheckingKey = false,
                                        isEnterKeyDialogVisible = true,
                                        enterKeyError = UiText.StringResource(R.string.invalid_recovery_key)
                                    )
                                }
                            }

                            else -> {
                                _state.update {
                                    it.copy(
                                        syncState = SyncState.Error(
                                            UiText.DynamicString(
                                                e.message ?: ""
                                            )
                                        ),
                                        isCheckingKey = false,
                                        pendingResolution = null
                                    )
                                }
                            }
                        }
                    }
            }.onFailure { _ ->
                _state.update {
                    it.copy(
                        syncState = SyncState.Error(UiText.StringResource(R.string.authentication_failed)),
                        isCheckingKey = false,
                        pendingResolution = null
                    )
                }
            }
        }
    }

    private fun loadStatus(provider: BackupProvider, updateLoading: Boolean = true) {
        viewModelScope.launch {
            if (updateLoading) _state.update { it.copy(isLoading = true) }
            backupRepository.getSyncStatus(provider)
                .onSuccess { status ->
                    _state.update {
                        it.copy(
                            syncStatus = status, //todo how to show in the ui?
                            isLoading = if (updateLoading) false else it.isLoading
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            syncState = SyncState.Error(UiText.DynamicString(e.message ?: "")),
                            isLoading = if (updateLoading) false else it.isLoading
                        )
                    }
                }
        }
    }

    private fun toggleCellularBackup() {
        viewModelScope.launch {
            settingsRepository.updateBackupOverCellular(!state.value.backupUsingCellular)
        }
    }
}
