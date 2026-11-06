package es.pile.core.domain.models

/**
 * Represents the current state of an OCR model download process.
 */
sealed class DownloadState {
    /**
     * Initial state when no download is active.
     */
    object Idle : DownloadState()

    /**
     * State emitted while downloading model assets.
     *
     * @property progress A value between 0.0 and 1.0 indicating overall download completion percentage.
     */
    data class Downloading(val progress: Float) : DownloadState()

    /**
     * State emitted when all requested model files have been downloaded successfully.
     */
    object Success : DownloadState()

    /**
     * State emitted when an error occurs during model downloading.
     *
     * @property message A human-readable error message explaining the failure cause.
     */
    data class Error(val message: String) : DownloadState()
}
