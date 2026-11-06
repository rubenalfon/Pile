package es.pile.core.domain.models.ocr

import es.pile.core.domain.models.DownloadState

/**
 * Represents the unified state of an OCR model group.
 *
 * @property group The underlying [OcrModelGroup] metadata.
 * @property isDownloaded Whether all ONNX files for this group exist locally.
 * @property isSelected Whether the user has selected this model group for OCR text extraction.
 * @property downloadState Current active download state if a download or error is in progress.
 */
data class OcrModelGroupState(
    val group: OcrModelGroup,
    val isDownloaded: Boolean,
    val isSelected: Boolean,
    val downloadState: DownloadState? = null
)
