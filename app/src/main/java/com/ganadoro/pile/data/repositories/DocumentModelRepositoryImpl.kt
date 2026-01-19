package com.ganadoro.pile.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.models.DocumentStatus
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class DocumentModelRepositoryImpl(
    private val databaseQueries: DatabaseQueries,
    private val ioDispatcher: CoroutineDispatcher
) : DocumentModelRepository {
    override val documentModels: Flow<List<DocumentModel>> =
        databaseQueries.selectAllDocumentModels().asFlow().mapToList(ioDispatcher)

    override suspend fun getAllDocumentModels(): List<DocumentModel> = withContext(ioDispatcher) {
        databaseQueries.selectAllDocumentModels().executeAsList()
    }

    override fun getDocumentModelById(id: String): Flow<DocumentModel?> =
        databaseQueries.selectDocumentModelById(id).asFlow().mapToOneOrNull(ioDispatcher)


    override fun getDocumentModelsByPileId(pileId: String): Flow<List<DocumentModel>> =
        databaseQueries.selectDocumentModelsByPileId(pileId).asFlow()
            .mapToList(ioDispatcher)


    override fun getDocumentModelsByStatus(documentStatus: DocumentStatus): Flow<List<DocumentModel>> =
        databaseQueries.selectDocumentModelsByStatus(documentStatus).asFlow()
            .mapToList(ioDispatcher)


    override suspend fun insertDocumentModel(documentModel: DocumentModel) {
        withContext(ioDispatcher) {
            databaseQueries.insertDocumentModel(
                id = documentModel.id,
                title = documentModel.title,
                imageIds = documentModel.imageIds,
                creationDateTime = documentModel.creationDateTime,
                modificationDateTime = documentModel.modificationDateTime,
                documentStatus = documentModel.documentStatus,
                documentPileIds = documentModel.documentPileIds,
                documentDetails = documentModel.documentDetails,
                documentNote = documentModel.documentNote,
                documentOrganizationIds = documentModel.documentOrganizationIds,
                isIncomingPdf = documentModel.isIncomingPdf
            )
        }
    }

    override suspend fun updateDocumentModel(documentModel: DocumentModel) {
        withContext(ioDispatcher) {
            databaseQueries.updateFullDocumentModel(
                title = documentModel.title,
                imageIds = documentModel.imageIds,
                modificationDateTime = documentModel.modificationDateTime,
                documentStatus = documentModel.documentStatus,
                documentPileIds = documentModel.documentPileIds,
                documentDetails = documentModel.documentDetails,
                documentNote = documentModel.documentNote,
                documentOrganizationIds = documentModel.documentOrganizationIds,
                isIncomingPdf = documentModel.isIncomingPdf,
                id = documentModel.id
            )
        }
    }

    override suspend fun deleteDocumentModel(id: String) {
        withContext(ioDispatcher) {
            databaseQueries.removeDocumentModel(id)
        }
    }
}