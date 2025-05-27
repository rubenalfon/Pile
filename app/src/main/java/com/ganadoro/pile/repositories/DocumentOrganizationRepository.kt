package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.DocumentOrganization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

interface DocumentOrganizationRepository {
    val documentOrganizations: Flow<List<DocumentOrganization>>
    suspend fun getAllDocumentOrganizations(): List<DocumentOrganization>
    suspend fun getDocumentOrganizationById(id: String): DocumentOrganization?
    suspend fun insertDocumentOrganization(documentOrganization: DocumentOrganization)
    suspend fun deleteDocumentOrganization(id: String)
}

class DocumentOrganizationRepositoryImpl(
    private val databaseQueries: DatabaseQueries,
) : DocumentOrganizationRepository {
    override val documentOrganizations: Flow<List<DocumentOrganization>> =
        databaseQueries.selectAllDocumentOrganizations().asFlow().mapToList(Dispatchers.IO)


    override suspend fun getAllDocumentOrganizations(): List<DocumentOrganization> {
       return  databaseQueries.selectAllDocumentOrganizations().executeAsList()
    }

    override suspend fun getDocumentOrganizationById(id: String): DocumentOrganization? {
        return databaseQueries.selectDocumentOrganizationById(id).executeAsOneOrNull()
    }

    override suspend fun insertDocumentOrganization(documentOrganization: DocumentOrganization) {
        databaseQueries.insertDocumentOrganization(
            id = documentOrganization.id,
            name = documentOrganization.name
        )
    }

    override suspend fun deleteDocumentOrganization(id: String) {
        databaseQueries.removeDocumentOrganization(id)
    }

}