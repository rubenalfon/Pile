package es.pile.features.pileDetail.di

import es.pile.features.pileDetail.domain.usecases.DeletePileUseCase
import es.pile.features.pileDetail.domain.usecases.UpdatePileUseCase
import es.pile.features.pileDetail.ui.PileDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf

import org.koin.dsl.module

val pileDetailModule = module {
    factoryOf(::UpdatePileUseCase)
    factoryOf(::DeletePileUseCase)

    viewModelOf(::PileDetailViewModel)
}