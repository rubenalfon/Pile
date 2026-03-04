package com.ganadoro.pile.features.addDocument.domain.useCases

import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.core.domain.models.DocumentStatusConstants
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.features.addDocument.domain.models.DocumentSaveException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class SaveDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository
) {
    suspend operator fun invoke(
        documentModel: DocumentModel,
        documentName: String
    ): Result<Unit> = runCatching {
        if (documentModel.documentStatus == DocumentStatusConstants.SAVED) {
            throw DocumentSaveException.AlreadySaved
        }

        if (documentName.isBlank()) {
            throw DocumentSaveException.EmptyName
        }

        val now = LocalDateTime.now()

        withContext(ioDispatcher) {
            val updatedDocument = documentModel.copy(
                title = documentName,
                creationDateTime = now,
                modificationDateTime = now,
                documentStatus = DocumentStatusConstants.SAVED,
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList()
            )

            documentModelRepository.updateDocumentModel(updatedDocument)
        }
    }
}