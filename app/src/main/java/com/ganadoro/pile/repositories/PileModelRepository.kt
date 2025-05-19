package com.ganadoro.pile.repositories

import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.PileModel


interface PileModelRepository {
    suspend fun getAllPileModels(): List<PileModel>
    suspend fun getPileModelById(id: String): PileModel?
    suspend fun insertPileModel(pileModel: PileModel)
    suspend fun updatePileModel(pileModel: PileModel)
    suspend fun deletePileModel(id: String)
}

class PileModelRepositoryImpl(
    private val databaseQueries: DatabaseQueries
) : PileModelRepository {
    override suspend fun getAllPileModels(): List<PileModel> {
        return databaseQueries.selectAllPileModels().executeAsList()
    }

    override suspend fun getPileModelById(id: String): PileModel? {
        return databaseQueries.selectPileModelById(id).executeAsOneOrNull()
    }

    override suspend fun insertPileModel(pileModel: PileModel) {
        databaseQueries.insertPileModel(
            id = pileModel.id,
            name = pileModel.name,
            icon = pileModel.icon,
            colorNumber = pileModel.colorNumber
        )
    }

    override suspend fun updatePileModel(pileModel: PileModel) {
        databaseQueries.updatePileModelName(pileModel.name, pileModel.id)
        databaseQueries.updatePileModelIcon(pileModel.icon, pileModel.id)
    }

    override suspend fun deletePileModel(id: String) {
        databaseQueries.removePileModel(id)
    }
}