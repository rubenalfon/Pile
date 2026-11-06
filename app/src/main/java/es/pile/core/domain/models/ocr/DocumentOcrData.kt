package es.pile.core.domain.models.ocr

import kotlinx.serialization.Serializable

/**
 * Container model representing the aggregated OCR data for a complete document, serialized as
 * JSON for storage.
 *
 * @property images A list of [ImageOcrData] containing OCR results for each page or image in the document.
 */
@Serializable
data class DocumentOcrData(
    val images: List<ImageOcrData>
)

/**
 * Represents the extracted OCR text for an individual image or PDF page within a document.
 *
 * @property id Unique identifier for the image or PDF page (e.g., image ID or `"pdf_page_X"`).
 * @property text The extracted raw text content from the image or page.
 */
@Serializable
data class ImageOcrData(
    val id: String,
    val text: String
)
