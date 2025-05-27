package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

interface DocumentModelRepository {    
    val documentModels: Flow<List<DocumentModel>>
    suspend fun getAllDocumentModels(): List<DocumentModel>
    suspend fun getDocumentModelById(id: String): DocumentModel?
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

    override suspend fun getDocumentModelById(id: String): DocumentModel? {
        return databaseQueries.selectDocumentModelById(id).executeAsOneOrNull()
    }

    override suspend fun insertDocumentModel(documentModel: DocumentModel) {
        databaseQueries.insertDocumentModel(
            id = documentModel.id,
            title = documentModel.title,
            creationDate = documentModel.creationDate,
            modificationDate = documentModel.modificationDate,
            documentPileIds = documentModel.documentPileIds,
            documentDetails = documentModel.documentDetails,
            documentOrganizationIds = documentModel.documentOrganizationIds
        )
    }

    override suspend fun updateDocumentModel(documentModel: DocumentModel) {
        databaseQueries.updateDocumentTitle(documentModel.title, documentModel.id)
        databaseQueries.updateDocumentModificationDate(documentModel.modificationDate, documentModel.id)
        databaseQueries.updateDocumentPileIds(documentModel.documentPileIds, documentModel.id)
        databaseQueries.updateDocumentDetails(documentModel.documentDetails, documentModel.id)
        databaseQueries.updateDocumentOrganizationIds(documentModel.documentOrganizationIds, documentModel.id)
    }

    override suspend fun deleteDocumentModel(id: String) {
        databaseQueries.removeDocumentModel(id)
    }
}