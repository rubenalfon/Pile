package com.ganadoro.pile.di

import com.ganadoro.pile.domain.usecases.ApplyImageFilterUseCase
import com.ganadoro.pile.domain.usecases.CreateDocumentUseCase
import com.ganadoro.pile.domain.usecases.CreatePileUseCase
import com.ganadoro.pile.domain.usecases.DeleteDocumentUseCase
import com.ganadoro.pile.domain.usecases.GetAvailableFiltersUseCase
import com.ganadoro.pile.domain.usecases.ManageTemporaryDocumentUseCase
import com.ganadoro.pile.domain.usecases.RequestBitmapLoadUseCase
import com.ganadoro.pile.domain.usecases.UpdateDocumentDetailsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::CreateDocumentUseCase)
    factoryOf(::ManageTemporaryDocumentUseCase)
    factoryOf(::CreatePileUseCase)
    factoryOf(::RequestBitmapLoadUseCase)
    factoryOf(::DeleteDocumentUseCase)
    factoryOf(::UpdateDocumentDetailsUseCase)
    factoryOf(::ApplyImageFilterUseCase)
    factoryOf(::GetAvailableFiltersUseCase)
}