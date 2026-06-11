package es.pile.core.domain.repositories

import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.models.ImageResolution
import es.pile.core.domain.models.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing and persisting user preferences.
 *
 * It provides a continuous stream of [UserSettings] and exposes methods to
 * perform atomic updates on specific preferences such as theme, image quality,
 * and AI configurations.
 */
interface SettingsRepository {
    /**
     * A [Flow] that emits the current [UserSettings] whenever they change.
     */
    val userSettings: Flow<UserSettings>

    /**
     * Updates the user settings with the provided [UserSettings] object.
     *
     * @param userSettings The new [UserSettings]
     */
    suspend fun updateUserSettings(userSettings: UserSettings)

    /**
     * Updates the application's visual theme.
     *
     * @param theme The new [AppTheme] to be applied (System, Light, or Dark).
     */
    suspend fun updateTheme(theme: AppTheme)

    /**
     * Updates the configuration for Material You color.
     *
     * @param enable Whether Material You color should be enabled or disabled.
     */
    suspend fun updateMaterialColor(enable: Boolean)

    /**
     * Updates the configuration for local AI features.
     *
     * @param enable Whether local AI should be enabled or disabled.
     */
    suspend fun updateLocalAi(enable: Boolean)

    /**
     * Updates the selected language model for local AI.
     *
     * @param model The new language model to be used.
     */
    suspend fun updateSelectedModel(model: String?)

    /**
     * Updates the image quality setting.
     *
     * @param resolution The new [ImageResolution] to be applied.
     */
    suspend fun updateImageResolution(resolution: ImageResolution)
}