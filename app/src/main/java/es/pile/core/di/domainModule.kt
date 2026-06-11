package es.pile.core.di

import es.pile.core.domain.useCases.CreatePileUseCase
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.core.domain.useCases.SaveImagesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::RequestBitmapLoadUseCase)
    factoryOf(::CreatePileUseCase)
    factoryOf(::SaveImagesUseCase)
}