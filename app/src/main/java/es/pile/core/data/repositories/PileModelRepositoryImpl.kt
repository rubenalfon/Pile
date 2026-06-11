package es.pile.core.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.pile.DatabaseQueries
import es.pile.PileModel
import es.pile.core.domain.repositories.PileModelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


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