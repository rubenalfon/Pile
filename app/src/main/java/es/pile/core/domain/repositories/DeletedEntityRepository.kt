package es.pile.core.domain.repositories

import es.pile.DeletedEntity

/**
 * Repository responsible for managing tombstone records of deleted entities.
 */
interface DeletedEntityRepository {
    /**
     * Retrieves all deleted entity records stored in the database.
     */
    suspend fun getAllDeletedEntities(): List<DeletedEntity>

    /**
     * Retrieves a set of deleted entity IDs filtered by entity type.
     *
     * @param type The type of entity (e.g., DOCUMENT, PILE, IMAGE).
     */
    suspend fun getDeletedEntityIdsByType(type: String): Set<String>

    /**
     * Registers an entity ID as deleted.
     *
     * @param id The unique identifier of the entity.
     * @param type The type of entity.
     */
    suspend fun insertDeletedEntity(id: String, type: String)

    /**
     * Clears a deletion tombstone record for an entity ID.
     *
     * @param id The unique identifier of the entity.
     */
    suspend fun removeDeletedEntity(id: String)
}
