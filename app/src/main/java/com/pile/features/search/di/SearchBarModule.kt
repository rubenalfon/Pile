package com.pile.features.search.di

import com.pile.features.search.ui.SearchBarViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchBarModule = module {
    viewModelOf(::SearchBarViewModel)
}