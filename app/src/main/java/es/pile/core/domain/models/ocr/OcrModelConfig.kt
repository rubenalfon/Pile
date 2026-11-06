package es.pile.core.domain.models.ocr

/**
 * Centralized configuration for all supported OCR (Optical Character Recognition) model groups.
 *
 * Each group bundles support for one or multiple languages, defining its unique ID and the
 * estimated size of the ONNX files required for download.
 */
object OcrModelConfig {
    val SUPPORTED_MODELS = listOf(
        OcrModelGroup(
            id = "english",
            languageCodes = listOf("en"),
            sizeInMb = 11f
        ),
        OcrModelGroup(
            id = "latin",
            languageCodes = listOf("ca", "de", "es", "eu", "fr", "gl", "it", "pt"),
            sizeInMb = 11f
        ),
        OcrModelGroup(
            id = "hindi",
            languageCodes = listOf("hi"),
            sizeInMb = 12f
        ),
        OcrModelGroup(
            id = "eslav",
            languageCodes = listOf("ru"),
            sizeInMb = 11f
        ),
        OcrModelGroup(
            id = "chinese",
            languageCodes = listOf("zh"),
            sizeInMb = 87f
        )
    )
}
