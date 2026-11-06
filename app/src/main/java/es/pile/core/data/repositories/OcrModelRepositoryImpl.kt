package es.pile.core.data.repositories

import android.content.Context
import es.pile.core.domain.models.DownloadState
import es.pile.core.domain.models.ocr.OcrModelConfig
import es.pile.core.domain.models.ocr.OcrModelFiles
import es.pile.core.domain.models.ocr.OcrModelGroup
import es.pile.core.domain.models.ocr.OcrModelGroupState
import es.pile.core.domain.repositories.OcrModelRepository
import es.pile.core.domain.repositories.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException

/**
 * Implementation of [OcrModelRepository] managing local ONNX model files and downloading assets from remote storage.
 *
 * @property appContext The application context used to resolve local storage directories.
 * @property settingsRepository Repository to read and persist user settings such as selected OCR models.
 * @property ioDispatcher Coroutine dispatcher for performing IO operations.
 * @property externalScope Coroutine scope for performing background tasks.
 */
class OcrModelRepositoryImpl(
    private val appContext: Context,
    private val settingsRepository: SettingsRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val externalScope: CoroutineScope
) : OcrModelRepository {

    private val modelsDir = File(appContext.filesDir, MODELS_DIR).apply {
        if (!exists()) mkdirs()
    }

    /**
     * Internal state flow representing the current download progress or errors for each model group.
     * Ephemeral state that lives only while the application process is alive.
     */
    private val downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())

    /**
     * Internal state flow caching the physical presence of model files on disk.
     * Initialized on creation and updated upon successful downloads or deletions.
     */
    private val downloadedModels = MutableStateFlow<Set<String>>(emptySet())

    init {
        externalScope.launch(ioDispatcher) {
            val initialSet = getSupportedModelGroups().map { it.id }.filter { modelId ->
                isModelDownloaded(modelId)
            }.toSet()
            downloadedModels.value = initialSet
        }
    }

    override val ocrModelGroupsState: Flow<List<OcrModelGroupState>> = combine(
        downloadedModels,
        downloadStates,
        settingsRepository.userSettings
    ) { downloaded, downloads, userSettings ->
        val selectedIds = userSettings.selectedOcrModelGroups

        getSupportedModelGroups().map { group ->
            OcrModelGroupState(
                group = group,
                isDownloaded = downloaded.contains(group.id),
                isSelected = selectedIds.contains(group.id),
                downloadState = downloads[group.id]
            )
        }
    }

    override val activeModelGroupIds: Flow<Set<String>> = combine(
        settingsRepository.userSettings,
        ocrModelGroupsState
    ) { userSettings, groups ->
        if (!userSettings.isLocalAiEnabled) {
            emptySet()
        } else {
            groups.asSequence()
                .filter { it.isDownloaded && it.isSelected }
                .map { it.group.id }
                .toSet()
        }
    }.distinctUntilChanged()

    override fun getSupportedModelGroups(): List<OcrModelGroup> = OcrModelConfig.SUPPORTED_MODELS

    override suspend fun getModelFiles(modelGroupId: String): OcrModelFiles? =
        withContext(ioDispatcher) {
            if (!isModelDownloaded(modelGroupId)) return@withContext null

            return@withContext OcrModelFiles(
                detectorFile = getDetectorFile(),
                recognizerFile = getRecognizerFile(modelGroupId),
                dictionaryFile = getDictionaryFile(modelGroupId)
            )
        }

    override suspend fun toggleModelGroupSelection(modelGroupId: String) {
        withContext(ioDispatcher) {
            if (!isModelDownloaded(modelGroupId)) return@withContext
            val currentSettings = settingsRepository.userSettings.first()
            val currentSelected = currentSettings.selectedOcrModelGroups
            val newSelected = if (modelGroupId in currentSelected) {
                currentSelected - modelGroupId
            } else {
                currentSelected + modelGroupId
            }
            settingsRepository.updateSelectedOcrModelGroups(newSelected)
        }
    }

    override suspend fun downloadModel(modelGroupId: String) = withContext(ioDispatcher) {
        downloadStates.update { it + (modelGroupId to DownloadState.Downloading(0f)) }

        try {
            require(OcrModelConfig.SUPPORTED_MODELS.any { it.id == modelGroupId }) {
                "Model group id $modelGroupId is not supported"
            }

            val filesToDownload = buildList {
                val detFile = getDetectorFile()
                if (!detFile.exists()) add(baseDetUrl to detFile)

                val recFile = getRecognizerFile(modelGroupId)
                if (!recFile.exists()) add(getRecUrl(modelGroupId) to recFile)

                val dictFile = getDictionaryFile(modelGroupId)
                if (!dictFile.exists()) add(getDictUrl(modelGroupId) to dictFile)
            }

            if (filesToDownload.isNotEmpty()) {
                val totalFiles = filesToDownload.size
                filesToDownload.forEachIndexed { index, (urlString, targetFile) ->
                    downloadFileToDisk(
                        urlString = urlString,
                        targetFile = targetFile,
                        onProgress = { fileProgress ->
                            val overallProgress = (index + fileProgress) / totalFiles
                            downloadStates.update { current ->
                                current + (modelGroupId to DownloadState.Downloading(overallProgress))
                            }
                        }
                    )
                }
            }

            downloadStates.update { it + (modelGroupId to DownloadState.Success) }
            downloadedModels.update { it + modelGroupId }

            val currentSettings = settingsRepository.userSettings.first()
            settingsRepository.updateSelectedOcrModelGroups(
                currentSettings.selectedOcrModelGroups + modelGroupId
            )

        } catch (e: CancellationException) {
            downloadStates.update { it - modelGroupId }
            throw e
        } catch (e: Exception) {
            Napier.e("OCR model download failed for $modelGroupId", e)
            val errorMessage = e.message ?: "Unknown error: ${e.javaClass.simpleName}"
            downloadStates.update { it + (modelGroupId to DownloadState.Error(errorMessage)) }
        }
    }

    private suspend fun downloadFileToDisk(
        urlString: String,
        targetFile: File,
        onProgress: (Float) -> Unit
    ) = coroutineScope {
        val tmpFile = File(modelsDir, "${targetFile.name}.tmp")
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("User-Agent", USER_AGENT)
        }

        try {
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP error ${connection.responseCode} downloading ${targetFile.name}")
            }

            val fileLength = connection.contentLength
            var lastReportedPercent = -1

            connection.inputStream.use { input ->
                FileOutputStream(tmpFile).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var totalBytesRead = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        currentCoroutineContext().ensureActive()
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (fileLength > 0) {
                            val progress = totalBytesRead.toFloat() / fileLength
                            val currentPercent = (progress * 100).toInt()

                            if (currentPercent > lastReportedPercent) {
                                lastReportedPercent = currentPercent
                                onProgress(progress)
                            }
                        }
                    }
                    output.flush()
                }
            }

            if (targetFile.exists()) targetFile.delete()
            if (!tmpFile.renameTo(targetFile)) {
                throw Exception("Failed to rename temporary file to ${targetFile.name}")
            }
        } catch (e: Exception) {
            if (tmpFile.exists()) tmpFile.delete()
            throw e
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun deleteModel(modelGroupId: String) = withContext(ioDispatcher) {
        if (getSupportedModelGroups().none { it.id == modelGroupId }) return@withContext
        getRecognizerFile(modelGroupId).delete()
        getDictionaryFile(modelGroupId).delete()
        downloadStates.update { it - modelGroupId }
        downloadedModels.update { it - modelGroupId }

        val currentSettings = settingsRepository.userSettings.first()
        if (modelGroupId in currentSettings.selectedOcrModelGroups) {
            settingsRepository.updateSelectedOcrModelGroups(currentSettings.selectedOcrModelGroups - modelGroupId)
        }
        
        if (downloadedModels.value.isEmpty()) {
            val detFile = getDetectorFile()
            if (detFile.exists()) {
                detFile.delete()
            }
        }
    }

    private fun isModelDownloaded(modelPrefix: String): Boolean {
        val detFile = getDetectorFile()
        val recFile = getRecognizerFile(modelPrefix)
        val dictFile = getDictionaryFile(modelPrefix)
        return detFile.exists() && recFile.exists() && dictFile.exists()
    }

    private fun getDetectorFile() = File(modelsDir, DETECTOR_FILE_NAME)
    private fun getRecognizerFile(modelPrefix: String) =
        File(modelsDir, "${modelPrefix}_$RECOGNIZER_FILE_SUFFIX")

    private fun getDictionaryFile(modelPrefix: String) =
        File(modelsDir, "${modelPrefix}_$DICTIONARY_FILE_SUFFIX")

    private val baseDetUrl =
        "https://huggingface.co/monkt/paddleocr-onnx/resolve/main/detection/v3/$DETECTOR_FILE_NAME"

    private fun getRecUrl(modelPrefix: String) =
        "https://huggingface.co/monkt/paddleocr-onnx/resolve/main/languages/$modelPrefix/$RECOGNIZER_FILE_SUFFIX"

    private fun getDictUrl(modelPrefix: String) =
        "https://huggingface.co/monkt/paddleocr-onnx/resolve/main/languages/$modelPrefix/$DICTIONARY_FILE_SUFFIX"

    companion object {
        private const val MODELS_DIR = "ocr_models"
        private const val DETECTOR_FILE_NAME = "det.onnx"
        private const val RECOGNIZER_FILE_SUFFIX = "rec.onnx"
        private const val DICTIONARY_FILE_SUFFIX = "dict.txt"
        private const val USER_AGENT = "Pile-Android"
        private const val TIMEOUT_MS = 15_000
    }
}
