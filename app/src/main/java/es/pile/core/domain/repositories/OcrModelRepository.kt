package es.pile.core.domain.repositories

import es.pile.core.domain.models.ocr.OcrModelFiles
import es.pile.core.domain.models.ocr.OcrModelGroup
import es.pile.core.domain.models.ocr.OcrModelGroupState
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing ONNX OCR language models.
 * Handles checking local availability, downloading model assets, deleting models,
 * and maintaining user model selections.
 */
interface OcrModelRepository {

    /**
     * Observable flow emitting the unified state of all supported OCR model groups,
     * including download status, progress, and user selection.
     */
    val ocrModelGroupsState: Flow<List<OcrModelGroupState>>

    /**
     * Observable flow emitting ONLY the model group IDs that are ready for OCR execution:
     * - Returns empty set if local AI is disabled globally.
     * - Returns models that are both downloaded and selected by the user when local AI is enabled.
     */
    val activeModelGroupIds: Flow<Set<String>>

    /**
     * Toggles the user selection for a model group.
     * Only has effect if the model group is downloaded.
     *
     * @param modelGroupId The ID of the model group to toggle.
     */
    suspend fun toggleModelGroupSelection(modelGroupId: String)

    /**
     * Retrieves the list of all available OCR model groups along with metadata such as estimated
     * download sizes.
     *
     * @return A list of supported [OcrModelGroup] configurations.
     */
    fun getSupportedModelGroups(): List<OcrModelGroup>

    /**
     * Retrieves local file handles for the detection model, recognition model, and dictionary
     * file for a specific model group.
     *
     * @param modelGroupId The ID of the model group.
     * @return An [OcrModelFiles] object containing the files if downloaded, or `null` if missing.
     */
    suspend fun getModelFiles(modelGroupId: String): OcrModelFiles?

    /**
     * Downloads missing ONNX detector, recognizer, and dictionary model files for the specified
     * model group. Automatically selects the model group upon successful download.
     *
     * @param modelGroupId The ID of the model group to download model files for.
     */
    suspend fun downloadModel(modelGroupId: String)

    /**
     * Deletes the recognition model and dictionary files associated with a model group from local
     * storage and removes it from user selection.
     *
     * @param modelGroupId The ID of the model group to remove.
     */
    suspend fun deleteModel(modelGroupId: String)
}
