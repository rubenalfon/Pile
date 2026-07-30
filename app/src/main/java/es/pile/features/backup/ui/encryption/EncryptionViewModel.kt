package es.pile.features.backup.ui.encryption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom

class EncryptionViewModel(
    private val settingsRepository: SettingsRepository
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
            val isEnabling = !state.value.isEncryptionOn
            if (isEnabling) {
                val existingKey = settingsRepository.getBackupMasterKey()
                if (existingKey == null) {
                    val newKey = generateMasterKey()
                    _state.update { it.copy(recoveryKey = newKey, isRecoveryKeyVisible = true) }
                } else {
                    settingsRepository.updateBackupEncryption(true)
                }
            } else {
                settingsRepository.updateBackupEncryption(false)
            }
        }
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
                settingsRepository.saveBackupMasterKey(key)
                settingsRepository.updateBackupEncryption(true)
                _state.update { it.copy(isRecoveryKeyVisible = false) }
            }
        }
    }

    private fun generateMasterKey(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
