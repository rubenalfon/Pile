package es.pile.core.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.pile.DatabaseQueries
import es.pile.DocumentImage
import es.pile.core.domain.repositories.DocumentImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class DocumentImageRepositoryImpl(
    private val databaseQueries: DatabaseQueries,
    private val ioDispatcher: CoroutineDispatcher
) : DocumentImageRepository {

    override suspend fun getAllDocumentImages(): List<DocumentImage> = withContext(ioDispatcher) {
        databaseQueries.selectAllDocumentImages().executeAsList()
    }

    override fun getDocumentImageById(id: String): Flow<DocumentImage?> =
        databaseQueries.selectDocumentImageById(id).asFlow().mapToOneOrNull(ioDispatcher)


    override suspend fun insertDocumentImage(documentImage: DocumentImage) {
        withContext(ioDispatcher) {
            databaseQueries.insertDocumentImage(
                documentImage.id,
                documentImage.isDraft,
                documentImage.crop,
                documentImage.filter,
                documentImage.rotation
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
                documentImage.id
            )
        }
    }

    override suspend fun deleteDocumentImage(id: String) {
        withContext(ioDispatcher) {
            databaseQueries.removeDocumentImage(id)
        }
    }
}