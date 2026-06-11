package es.pile.core.domain.repositories

import es.pile.DocumentImage
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing [DocumentImage] data.
 * Handles persistence and retrieval of image-specific metadata such as cropping, filters, and rotation.
 */
interface DocumentImageRepository {
    
    /**
     * Retrieves all document images currently stored in the database.
     * 
     * @return A list of all [DocumentImage] records.
     */
    suspend fun getAllDocumentImages(): List<DocumentImage>

    /**
     * Retrieves a specific document image by its unique identifier as a reactive stream.
     * 
     * @param id The unique identifier of the image.
     * @return A [Flow] emitting the [DocumentImage] if found, or null otherwise.
     */
    fun getDocumentImageById(id: String): Flow<DocumentImage?>

    /**
     * Persists a new document image record in the database.
     * 
     * @param documentImage The image metadata to insert.
     */
    suspend fun insertDocumentImage(documentImage: DocumentImage)

    /**
     * Updates an existing document image record in the database.
     * 
     * @param documentImage The updated image metadata.
     */
    suspend fun updateDocumentImage(documentImage: DocumentImage)

    /**
     * Removes a document image record from the database.
     * 
     * @param id The unique identifier of the image to delete.
     */
    suspend fun deleteDocumentImage(id: String)
}
