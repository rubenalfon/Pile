package es.pile.core.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.core.domain.models.AppPreferences
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.AppPreferencesRepository
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

class MainViewModel(
    settingsRepository: SettingsRepository,
    appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val state: StateFlow<MainState> = combine(
        settingsRepository.userSettings,
        appPreferencesRepository.appPreferences
    ) { settings, appPrefs ->
        MainState(
            settings = settings,
            appPreferences = appPrefs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking {
            MainState(
                settings = settingsRepository.userSettings.first(),
                appPreferences = appPreferencesRepository.appPreferences.first(),
                isLoading = false
            )
        }
    )
}

data class MainState(
    val settings: UserSettings = UserSettings(),
    val appPreferences: AppPreferences = AppPreferences(),
    val isLoading: Boolean = true
)
