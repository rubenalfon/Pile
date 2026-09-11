package es.pile.core.data.repositories

import es.pile.DatabaseQueries
import es.pile.DeletedEntity
import es.pile.core.domain.repositories.DeletedEntityRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class DeletedEntityRepositoryImpl(
    private val databaseQueries: DatabaseQueries,
    private val ioDispatcher: CoroutineDispatcher
) : DeletedEntityRepository {

    override suspend fun getAllDeletedEntities(): List<DeletedEntity> = withContext(ioDispatcher) {
        try {
            databaseQueries.selectAllDeletedEntities().executeAsList()
        } catch (e: Exception) {
            Napier.w("DeletedEntity table query failed", e)
            emptyList()
        }
    }

    override suspend fun getDeletedEntityIdsByType(type: String): Set<String> = withContext(ioDispatcher) {
        try {
            databaseQueries.selectDeletedEntitiesByType(type).executeAsList().map { it.id }.toSet()
        } catch (e: Exception) {
            Napier.w("DeletedEntity table query failed for type $type", e)
            emptySet()
        }
    }

    override suspend fun insertDeletedEntity(id: String, type: String) {
        withContext(ioDispatcher) {
            try {
                databaseQueries.insertDeletedEntity(id, type, LocalDateTime.now())
            } catch (e: Exception) {
                Napier.w("Failed to insert DeletedEntity for $id", e)
            }
        }
    }

    override suspend fun removeDeletedEntity(id: String) {
        withContext(ioDispatcher) {
            try {
                databaseQueries.removeDeletedEntity(id)
            } catch (e: Exception) {
                Napier.w("Failed to remove DeletedEntity for $id", e)
            }
        }
    }
}
