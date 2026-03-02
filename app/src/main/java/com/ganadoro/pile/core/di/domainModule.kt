package com.ganadoro.pile.core.di

import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::RequestBitmapLoadUseCase)

}