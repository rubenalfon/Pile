package com.ganadoro.pile.core.activities.di

import com.ganadoro.pile.core.activities.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val activitiesModule = module {
    viewModelOf(::MainViewModel)
}

