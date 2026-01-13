package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.models.DocumentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

interface DocumentModelRepository {    
    val documentModels: Flow<List<DocumentModel>>
    suspend fun getAllDocumentModels(): List<DocumentModel>
    suspend fun getDocumentModelById(id: String): Flow<DocumentModel?>
    suspend fun getDocumentModelsByPileId(pileId: String): Flow<List<DocumentModel>>

    suspend fun getDocumentModelsByStatus(documentStatus: DocumentStatus): Flow<List<DocumentModel>>
    suspend fun insertDocumentModel(documentModel: DocumentModel)
    suspend fun updateDocumentModel(documentModel: DocumentModel)
    suspend fun deleteDocumentModel(id: String)
}

class DocumentModelRepositoryImpl(
    private val databaseQueries: DatabaseQueries
) : DocumentModelRepository {
    override val documentModels: Flow<List<DocumentModel>> =
        databaseQueries.selectAllDocumentModels().asFlow().mapToList(Dispatchers.IO)

    override suspend fun getAllDocumentModels(): List<DocumentModel> {
        return databaseQueries.selectAllDocumentModels().executeAsList()
    }

    override suspend fun getDocumentModelById(id: String): Flow<DocumentModel?> {
        return databaseQueries.selectDocumentModelById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override suspend fun getDocumentModelsByPileId(pileId: String): Flow<List<DocumentModel>> {
        return databaseQueries.selectDocumentModelsByPileId(pileId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun getDocumentModelsByStatus(documentStatus: DocumentStatus): Flow<List<DocumentModel>> {
        return databaseQueries.selectDocumentModelsByStatus(documentStatus).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertDocumentModel(documentModel: DocumentModel) {
        databaseQueries.insertDocumentModel(
            id = documentModel.id,
            title = documentModel.title,
            imageIds = documentModel.imageIds,
            creationDate = documentModel.creationDate,
            modificationDate = documentModel.modificationDate,
            documentStatus = documentModel.documentStatus,
            documentPileIds = documentModel.documentPileIds,
            documentDetails = documentModel.documentDetails,
            documentNote = documentModel.documentNote,
            documentOrganizationIds = documentModel.documentOrganizationIds,
            isIncomingPdf = documentModel.isIncomingPdf
        )
    }

    override suspend fun updateDocumentModel(documentModel: DocumentModel) {
        databaseQueries.updateDocumentTitle(documentModel.title, documentModel.id)
        databaseQueries.updateDocumentImageIds(documentModel.imageIds, documentModel.id)
        databaseQueries.updateDocumentModificationDate(documentModel.modificationDate, documentModel.id)
        databaseQueries.updateDocumentStatus(documentModel.documentStatus, documentModel.id)
        databaseQueries.updateDocumentPileIds(documentModel.documentPileIds, documentModel.id)
        databaseQueries.updateDocumentDetails(documentModel.documentDetails, documentModel.id)
        databaseQueries.updateDocumentNote(documentModel.documentNote, documentModel.id)
        databaseQueries.updateDocumentOrganizationIds(documentModel.documentOrganizationIds, documentModel.id)
        databaseQueries.updateDocumentIsIncomingPdf(documentModel.isIncomingPdf, documentModel.id)
    }

    override suspend fun deleteDocumentModel(id: String) {
        databaseQueries.removeDocumentModel(id)
    }
}