package com.ganadoro.pile.core.di

import com.ganadoro.pile.core.domain.useCases.CreatePileUseCase
import com.ganadoro.pile.core.domain.useCases.ManageDocumentPileUseCase
import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.ganadoro.pile.core.domain.useCases.SaveImagesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::RequestBitmapLoadUseCase)
    factoryOf(::CreatePileUseCase)
    factoryOf(::ManageDocumentPileUseCase)
    factoryOf(::SaveImagesUseCase)
}