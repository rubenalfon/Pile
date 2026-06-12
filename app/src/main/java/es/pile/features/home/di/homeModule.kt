package es.pile.features.home.di

import es.pile.features.home.data.schedulers.WorkManagerCleanupScheduler
import es.pile.features.home.data.workers.CleanupWorker
import es.pile.features.home.domain.schedulers.CleanupScheduler
import es.pile.features.home.domain.useCases.CreateDocumentUseCase
import es.pile.features.home.domain.useCases.GetHomeDataUseCase
import es.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase
import es.pile.features.home.ui.HomeViewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::CreateDocumentUseCase)
    factoryOf(::ManageTemporaryDocumentUseCase)
    factoryOf(::GetHomeDataUseCase)

    workerOf(::CleanupWorker)
    single<CleanupScheduler> {
        WorkManagerCleanupScheduler(get())
    }

    viewModelOf(::HomeViewModel)
}