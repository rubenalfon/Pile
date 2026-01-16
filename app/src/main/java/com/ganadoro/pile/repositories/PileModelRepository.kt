package com.ganadoro.pile.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ganadoro.pile.DatabaseQueries
import com.ganadoro.pile.PileModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


interface PileModelRepository {
    val pileModels: Flow<List<PileModel>>
    suspend fun getAllPileModels(): List<PileModel>
    fun getPileModelById(id: String): Flow<PileModel?>
    fun getPileModelsByIds(ids: List<String>): Flow<List<PileModel>>
    suspend fun insertPileModel(pileModel: PileModel)
    suspend fun updatePileModel(pileModel: PileModel)
    suspend fun deletePileModel(id: String)
}

class PileModelRepositoryImpl(
    private val databaseQueries: DatabaseQueries,
    private val ioDispatcher: CoroutineDispatcher
) : PileModelRepository {

    override val pileModels: Flow<List<PileModel>> = databaseQueries.selectAllPileModels()
        .asFlow()
        .mapToList(ioDispatcher)


    override suspend fun getAllPileModels(): List<PileModel> = withContext(ioDispatcher) {
        databaseQueries.selectAllPileModels().executeAsList()
    }

    override fun getPileModelById(id: String): Flow<PileModel?> =
        databaseQueries.selectPileModelById(id).asFlow().mapToOneOrNull(ioDispatcher)

    override fun getPileModelsByIds(ids: List<String>): Flow<List<PileModel>> =
        databaseQueries.selectPileModelsById(ids).asFlow().mapToList(ioDispatcher)


    override suspend fun insertPileModel(pileModel: PileModel) {
        withContext(ioDispatcher) {
            databaseQueries.insertPileModel(
                id = pileModel.id,
                name = pileModel.name,
                iconId = pileModel.iconId,
                colorNumber = pileModel.colorNumber
            )
        }
    }

    override suspend fun updatePileModel(pileModel: PileModel) {
        withContext(ioDispatcher) {
            databaseQueries.updatePileModel(
                pileModel.name,
                pileModel.iconId,
                pileModel.colorNumber,
                pileModel.id
            )
        }
    }

    override suspend fun deletePileModel(id: String) {
        withContext(ioDispatcher) {
            databaseQueries.removePileModel(id)
        }
    }
}