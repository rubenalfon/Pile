package es.pile.core.domain.repositories

import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.models.DocumentStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing [DocumentModel] data.
 * This is the primary data source for document metadata, including titles, status, and associations with piles.
 */
interface DocumentModelRepository {
    
    /**
     * A reactive stream of all document models in the database.
     */
    val documentModels: Flow<List<DocumentModel>>

    /**
     * Retrieves all document models currently stored in the database.
     * 
     * @return A list of all [DocumentModel] records.
     */
    suspend fun getAllDocumentModels(): List<DocumentModel>

    /**
     * Retrieves a specific document model by its unique identifier.
     * 
     * @param id The unique identifier of the document.
     * @return A [Flow] emitting the [DocumentModel] if found, or null otherwise.
     */
    fun getDocumentModelById(id: String): Flow<DocumentModel?>

    /**
     * Retrieves all documents associated with a specific pile.
     * 
     * @param pileId The unique identifier of the pile.
     * @return A [Flow] emitting a list of [DocumentModel]s belonging to the pile.
     */
    fun getDocumentModelsByPileId(pileId: String): Flow<List<DocumentModel>>

    /**
     * Retrieves all documents filtered by their current status.
     * 
     * @param documentStatus The status to filter by.
     * @return A [Flow] emitting a list of [DocumentModel]s matching the status.
     */
    fun getDocumentModelsByStatus(documentStatus: DocumentStatus): Flow<List<DocumentModel>>

    /**
     * Persists a new document model record in the database.
     * 
     * @param documentModel The document metadata to insert.
     */
    suspend fun insertDocumentModel(documentModel: DocumentModel)

    /**
     * Updates an existing document model record in the database.
     * 
     * @param documentModel The updated document metadata.
     */
    suspend fun updateDocumentModel(documentModel: DocumentModel)

    /**
     * Updates the title of a document.
     */
    suspend fun updateTitle(id: String, title: String)

    /**
     * Updates the note of a document.
     */
    suspend fun updateNote(id: String, note: String)

    /**
     * Updates the details of a document.
     */
    suspend fun updateDetails(id: String, details: List<DocumentDetail>)

    /**
     * Removes a document model record from the database.
     * 
     * @param id The unique identifier of the document to delete.
     */
    suspend fun deleteDocumentModel(id: String)
}
