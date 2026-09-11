package es.pile.features.backup.ui.encryption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom

class EncryptionViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(EncryptionState())
    val state: StateFlow<EncryptionState> = _state.asStateFlow()

    init {
        settingsRepository.userSettings.onEach { settings ->
            _state.update {
                it.copy(isEncryptionOn = settings.isBackupEncryptionEnabled)
            }
        }.launchIn(viewModelScope)
    }

    fun handleEvent(event: EncryptionEvent) {
        when (event) {
            EncryptionEvent.OnToggleEncryption -> {
                if (state.value.isEncryptionOn) {
                    _state.update { it.copy(isDisableAlertVisible = true) }
                } else {
                    toggleEncryption()
                }
            }

            EncryptionEvent.OnShowRecoveryKeyClicked -> showRecoveryKey()
            EncryptionEvent.OnHideRecoveryKey -> _state.update { it.copy(isRecoveryKeyVisible = false) }
            EncryptionEvent.OnRecoveryKeyConfirmed -> confirmRecoveryKey()
            EncryptionEvent.OnShowDisableAlert -> _state.update { it.copy(isDisableAlertVisible = true) }
            EncryptionEvent.OnHideDisableAlert -> _state.update { it.copy(isDisableAlertVisible = false) }
            EncryptionEvent.OnConfirmDisable -> {
                _state.update { it.copy(isDisableAlertVisible = false) }
                toggleEncryption()
            }

            EncryptionEvent.OnBackClicked -> {}
        }
    }

    private fun toggleEncryption() {
        viewModelScope.launch {
            if (state.value.isEncryptionOn) {
                disableEncryptionAndReSync()
            } else {
                enableEncryption()
            }
        }
    }

    private fun enableEncryption() {
        val newKey = generateMasterKey()
        _state.update { it.copy(recoveryKey = newKey, isRecoveryKeyVisible = true) }
        // When user confirms key -> confirmRecoveryKey()
    }

    private suspend fun disableEncryptionAndReSync() {
        _state.update { it.copy(isLoading = true) }
        settingsRepository.updateBackupEncryption(false)
        settingsRepository.removeBackupMasterKey()
        wipeAndSyncCloud()
        _state.update { it.copy(isLoading = false) }
    }

    private fun showRecoveryKey() {
        viewModelScope.launch {
            val key = settingsRepository.getBackupMasterKey()
            _state.update { it.copy(recoveryKey = key, isRecoveryKeyVisible = true) }
        }
    }

    private fun confirmRecoveryKey() {
        val key = state.value.recoveryKey
        if (key != null) {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, isRecoveryKeyVisible = false) }
                settingsRepository.saveBackupMasterKey(key)
                settingsRepository.updateBackupEncryption(true)
                wipeAndSyncCloud()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun wipeAndSyncCloud() {
        val selectedProviderName =
            settingsRepository.userSettings.first().selectedBackupProviderName
        val provider = backupRepository.availableProviders.find { it.name == selectedProviderName }
        if (provider != null) {
            backupRepository.wipeCloudData(provider)
            syncManager.requestSync(force = true)
        }
    }

    private fun generateMasterKey(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
