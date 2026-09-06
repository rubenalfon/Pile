package es.pile.features.backup.ui.wipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.R
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.sync.SyncManager
import es.pile.core.ui.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WipeCloudViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(WipeCloudState())
    val state: StateFlow<WipeCloudState> = _state.asStateFlow()

    fun handleEvent(event: WipeCloudEvent) {
        when (event) {
            WipeCloudEvent.OnConfirmWipe -> wipeCloudData()
            WipeCloudEvent.OnBackClicked -> {}
        }
    }

    private fun wipeCloudData() {
        if (state.value.isWiping) return

        viewModelScope.launch {
            _state.update { it.copy(isWiping = true, error = null) }

            val selectedProviderName = settingsRepository.userSettings.first().selectedBackupProviderName
            val provider = backupRepository.availableProviders.find { it.name == selectedProviderName }

            if (provider != null) {
                backupRepository.wipeCloudData(provider)
                    .onSuccess {
                        syncManager.requestSync(force = true) // TODO: No
                        _state.update { it.copy(isWiping = false, isSuccess = true) }
                    }
                    .onFailure { e ->
                        _state.update {
                            it.copy(
                                isWiping = false,
                                error = e.message?.let { msg -> UiText.DynamicString(msg) }
                                    ?: UiText.StringResource(R.string.authentication_failed)
                            )
                        }
                    }
            } else {
                _state.update {
                    it.copy(
                        isWiping = false,
                        error = UiText.StringResource(R.string.authentication_failed)
                    )
                }
            }
        }
    }
}
