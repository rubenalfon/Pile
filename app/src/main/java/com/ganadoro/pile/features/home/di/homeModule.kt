package com.ganadoro.pile.features.home.di

import com.ganadoro.pile.core.domain.useCases.CreatePileUseCase
import com.ganadoro.pile.features.home.data.schedulers.WorkManagerCleanupScheduler
import com.ganadoro.pile.features.home.data.workers.CleanupWorker
import com.ganadoro.pile.features.home.domain.schedulers.CleanupScheduler
import com.ganadoro.pile.features.home.domain.useCases.CreateDocumentUseCase
import com.ganadoro.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase
import com.ganadoro.pile.features.home.ui.HomeViewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::CreateDocumentUseCase)
    factoryOf(::ManageTemporaryDocumentUseCase)
    factoryOf(::CreatePileUseCase)

    workerOf(::CleanupWorker)
    single<CleanupScheduler> {
        WorkManagerCleanupScheduler(get())
    }

    viewModelOf(::HomeViewModel)
}