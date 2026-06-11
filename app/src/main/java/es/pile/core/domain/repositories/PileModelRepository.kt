package es.pile.core.domain.repositories

import es.pile.PileModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for managing [PileModel] data.
 * Handles the organization of documents into different Piles,
 * including their names, icons, and colors.
 */
interface PileModelRepository {

    /**
     * A reactive stream of all available piles in the database.
     */
    val pileModels: Flow<List<PileModel>>

    /**
     * Retrieves all piles currently stored in the database.
     * 
     * @return A list of all [PileModel] records.
     */
    suspend fun getAllPileModels(): List<PileModel>

    /**
     * Retrieves a specific pile by its unique identifier.
     * 
     * @param id The unique identifier of the pile.
     * @return A [Flow] emitting the [PileModel] if found, or null otherwise.
     */
    fun getPileModelById(id: String): Flow<PileModel?>

    /**
     * Retrieves a list of specific piles based on a list of IDs.
     * 
     * @param ids The list of unique identifiers to look for.
     * @return A [Flow] emitting the list of matching [PileModel]s.
     */
    fun getPileModelsByIds(ids: List<String>): Flow<List<PileModel>>

    /**
     * Persists a new pile record in the database.
     * 
     * @param pileModel The pile metadata to insert.
     */
    suspend fun insertPileModel(pileModel: PileModel)

    /**
     * Updates an existing pile's information (name, icon, or color).
     * 
     * @param pileModel The updated pile metadata.
     */
    suspend fun updatePileModel(pileModel: PileModel)

    /**
     * Removes a pile record from the database.
     * 
     * @param id The unique identifier of the pile to delete.
     */
    suspend fun deletePileModel(id: String)
}
