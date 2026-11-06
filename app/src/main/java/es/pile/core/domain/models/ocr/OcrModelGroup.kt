package es.pile.core.domain.models.ocr

import java.util.Locale

/**
 * Represents a downloadable group of language models used for Offline Text Recognition (OCR).
 * Some OCR models (like "latin") bundle support for multiple languages together.
 *
 * @property id The unique identifier/prefix of the model group (e.g., "latin", "english", "chinese").
 * @property languageCodes The ISO 639-1 language codes supported by this model group.
 * @property sizeInMb The estimated size of the OCR model files in megabytes.
 */
data class OcrModelGroup(
    val id: String,
    val languageCodes: List<String>,
    val sizeInMb: Float
) {
    /**
     * Gets a formatted display name for this model group.
     * It translates each language code to its native or current locale display name.
     * Languages that are present in the user's device configuration ([deviceLanguageCodes]) are sorted first.
     *
     * @param deviceLanguageCodes A list of ISO 639-1 language codes currently configured on the user's device.
     */
    fun getDisplayName(deviceLanguageCodes: List<String> = emptyList()): String {
        val deviceLangSet = deviceLanguageCodes.toSet()

        val sortedCodes = languageCodes.sortedWith(
            compareByDescending<String> { it in deviceLangSet }
                .thenBy { getLanguageDisplayName(it) }
        )

        return sortedCodes.joinToString(", ") { getLanguageDisplayName(it) }
    }

    private fun getLanguageDisplayName(languageCode: String): String {
        val locale = Locale.forLanguageTag(languageCode)
        return locale.displayName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
        }
    }
}
