package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.PileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow


interface PileModelRepository {
    val pileModels: Flow<List<PileModel>>
    suspend fun getAllPileModels(): List<PileModel>
    suspend fun getPileModelById(id: String): PileModel?
    suspend fun getPileModelsByIds(ids: List<String>): Flow<List<PileModel>>
    suspend fun insertPileModel(pileModel: PileModel)
    suspend fun updatePileModel(pileModel: PileModel)
    suspend fun deletePileModel(id: String)
}

class PileModelRepositoryImpl(
    private val databaseQueries: DatabaseQueries
) : PileModelRepository {

    override val pileModels: Flow<List<PileModel>> = databaseQueries.selectAllPileModels()
        .asFlow()
        .mapToList(Dispatchers.IO)


    override suspend fun getAllPileModels(): List<PileModel> {
        return databaseQueries.selectAllPileModels().executeAsList()
    }

    override suspend fun getPileModelById(id: String): PileModel? {
        return databaseQueries.selectPileModelById(id).executeAsOneOrNull()
    }

    override suspend fun getPileModelsByIds(ids: List<String>): Flow<List<PileModel>> {
        return databaseQueries.selectPileModelsById(ids).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertPileModel(pileModel: PileModel) {
        databaseQueries.insertPileModel(
            id = pileModel.id,
            name = pileModel.name.trim(),
            iconId = pileModel.iconId,
            colorNumber = pileModel.colorNumber
        )
    }

    override suspend fun updatePileModel(pileModel: PileModel) {
        databaseQueries.updatePileModelName(pileModel.name.trim(), pileModel.id)
        databaseQueries.updatePileModelIcon(pileModel.iconId, pileModel.id)
        databaseQueries.updatePileModelColor(pileModel.colorNumber, pileModel.id)
    }

    override suspend fun deletePileModel(id: String) {
        databaseQueries.removePileModel(id)
    }
}