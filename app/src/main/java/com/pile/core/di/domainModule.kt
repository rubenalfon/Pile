package com.pile.core.di

import com.pile.core.domain.useCases.CreatePileUseCase
import com.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.pile.core.domain.useCases.SaveImagesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::RequestBitmapLoadUseCase)
    factoryOf(::CreatePileUseCase)
    factoryOf(::SaveImagesUseCase)
}