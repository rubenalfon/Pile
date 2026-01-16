package com.ganadoro.pile.di

import com.ganadoro.pile.domain.usecase.CreateDocumentUseCase
import com.ganadoro.pile.domain.usecase.CreatePileUseCase
import com.ganadoro.pile.domain.usecase.DeleteDocumentUseCase
import com.ganadoro.pile.domain.usecase.ManageTemporaryDocumentUseCase
import com.ganadoro.pile.domain.usecase.RequestBitmapLoadUseCase
import com.ganadoro.pile.domain.usecase.UpdateDocumentDetailsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::CreateDocumentUseCase)
    factoryOf(::ManageTemporaryDocumentUseCase)
    factoryOf(::CreatePileUseCase)
    factoryOf(::RequestBitmapLoadUseCase)
    factoryOf(::DeleteDocumentUseCase)
    factoryOf(::UpdateDocumentDetailsUseCase)
}