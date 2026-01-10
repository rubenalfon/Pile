package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

interface DocumentImageRepository {
    suspend fun getAllDocumentImages(): List<DocumentImage>
    suspend fun getDocumentImageById(id: String): Flow<DocumentImage?>
    suspend fun insertDocumentImage(documentImage: DocumentImage)
    suspend fun updateDocumentImage(documentImage: DocumentImage)
    suspend fun deleteDocumentImage(id: String)
}

class DocumentImageRepositoryImpl(
    private val databaseQueries: DatabaseQueries
) : DocumentImageRepository {

    override suspend fun getAllDocumentImages(): List<DocumentImage> {
        return databaseQueries.selectAllDocumentImages().executeAsList()
    }

    override suspend fun getDocumentImageById(id: String): Flow<DocumentImage?> {
        return databaseQueries.selectDocumentImageById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override suspend fun insertDocumentImage(documentImage: DocumentImage) {
        databaseQueries.insertDocumentImage(
            documentImage.id,
            documentImage.crop,
            documentImage.filter,
            documentImage.rotation
        )
    }

    override suspend fun updateDocumentImage(documentImage: DocumentImage) {
        databaseQueries.updateCrop(documentImage.crop, documentImage.id)
        databaseQueries.updateFilter(documentImage.filter, documentImage.id)
        databaseQueries.updateRotation(documentImage.rotation, documentImage.id)
    }

    override suspend fun deleteDocumentImage(id: String) {
        databaseQueries.removeDocumentImage(id)
    }
}