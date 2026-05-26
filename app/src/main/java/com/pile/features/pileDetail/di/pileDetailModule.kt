package com.pile.features.pileDetail.di

import com.pile.features.pileDetail.domain.usecases.DeletePileUseCase
import com.pile.features.pileDetail.domain.usecases.UpdatePileUseCase
import com.pile.features.pileDetail.ui.PileDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf

import org.koin.dsl.module

val pileDetailModule = module {
    factoryOf(::UpdatePileUseCase)
    factoryOf(::DeletePileUseCase)

    viewModelOf(::PileDetailViewModel)
}