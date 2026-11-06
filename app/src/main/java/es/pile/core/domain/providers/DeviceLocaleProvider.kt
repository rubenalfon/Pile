package es.pile.core.domain.providers

/**
 * Provider interface to obtain the language preferences configured on the user's device.
 */
interface DeviceLocaleProvider {
    /**
     * Obtains the list of language codes currently configured on the device.
     *
     * @return A list of distinct two-letter ISO 639-1 language codes (e.g., ["en", "es"])
     * configured on the device, ordered by the user's preference.
     */
    fun getLanguageCodes(): List<String>
}
