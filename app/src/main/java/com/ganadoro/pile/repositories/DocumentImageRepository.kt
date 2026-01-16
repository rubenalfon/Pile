package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DocumentImageRepository {
    suspend fun getAllDocumentImages(): List<DocumentImage>
    fun getDocumentImageById(id: String): Flow<DocumentImage?>
    suspend fun insertDocumentImage(documentImage: DocumentImage)
    suspend fun updateDocumentImage(documentImage: DocumentImage)
    suspend fun deleteDocumentImage(id: String)
}

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
                documentImage.crop,
                documentImage.filter,
                documentImage.rotation
            )
        }
    }

    override suspend fun updateDocumentImage(documentImage: DocumentImage) {
        withContext(ioDispatcher) {
            databaseQueries.updateFullDocumentImage(
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