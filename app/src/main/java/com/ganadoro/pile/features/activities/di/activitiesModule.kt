package com.ganadoro.pile.features.activities.di

import com.ganadoro.pile.features.activities.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val activitiesModule = module {
    viewModelOf(::MainViewModel)
}

