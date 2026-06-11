package es.pile.core.activities.di

import es.pile.core.activities.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val activitiesModule = module {
    viewModelOf(::MainViewModel)
}

