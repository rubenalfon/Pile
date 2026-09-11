package es.pile.core.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.pile.DatabaseQueries
import es.pile.DocumentImage
import es.pile.core.domain.models.DeletedEntityType
import es.pile.core.domain.repositories.DeletedEntityRepository
import es.pile.core.domain.repositories.DocumentImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class DocumentImageRepositoryImpl(
    private val databaseQueries: DatabaseQueries,
    private val deletedEntityRepository: DeletedEntityRepository,
    private val ioDispatcher: CoroutineDispatcher
) : DocumentImageRepository {

    override suspend fun getAllDocumentImages(): List<DocumentImage> = withContext(ioDispatcher) {
        databaseQueries.selectAllDocumentImages().executeAsList()
    }

    override fun getDocumentImageById(id: String): Flow<DocumentImage?> =
        databaseQueries.selectDocumentImageById(id).asFlow().mapToOneOrNull(ioDispatcher)


    override suspend fun insertDocumentImage(documentImage: DocumentImage) {
        withContext(ioDispatcher) {
            deletedEntityRepository.removeDeletedEntity(documentImage.id)
            databaseQueries.insertDocumentImage(
                documentImage.id,
                documentImage.isDraft,
                documentImage.crop,
                documentImage.filter,
                documentImage.rotation,
                documentImage.modificationDateTime
            )
        }
    }

    override suspend fun updateDocumentImage(documentImage: DocumentImage) {
        withContext(ioDispatcher) {
            databaseQueries.updateFullDocumentImage(
                documentImage.isDraft,
                documentImage.crop,
                documentImage.filter,
                documentImage.rotation,
                modificationDateTime = LocalDateTime.now(),
                documentImage.id
            )
        }
    }

    override suspend fun deleteDocumentImage(id: String) {
        withContext(ioDispatcher) {
            deletedEntityRepository.insertDeletedEntity(id, DeletedEntityType.IMAGE)
            databaseQueries.removeDocumentImage(id)
        }
    }
}