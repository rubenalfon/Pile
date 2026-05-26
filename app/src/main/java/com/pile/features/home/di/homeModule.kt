package com.pile.features.home.di

import com.pile.features.home.data.schedulers.WorkManagerCleanupScheduler
import com.pile.features.home.data.workers.CleanupWorker
import com.pile.features.home.domain.schedulers.CleanupScheduler
import com.pile.features.home.domain.useCases.CreateDocumentUseCase
import com.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase
import com.pile.features.home.ui.HomeViewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::CreateDocumentUseCase)
    factoryOf(::ManageTemporaryDocumentUseCase)

    workerOf(::CleanupWorker)
    single<CleanupScheduler> {
        WorkManagerCleanupScheduler(get())
    }

    viewModelOf(::HomeViewModel)
}