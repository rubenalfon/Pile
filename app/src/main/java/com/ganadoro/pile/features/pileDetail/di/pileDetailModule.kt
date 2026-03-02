package com.ganadoro.pile.features.pileDetail.di

import com.ganadoro.pile.features.pileDetail.ui.PileDetailViewModel
import org.koin.core.module.dsl.viewModelOf

import org.koin.dsl.module

val pileDetailModule = module {
    viewModelOf(::PileDetailViewModel)
}