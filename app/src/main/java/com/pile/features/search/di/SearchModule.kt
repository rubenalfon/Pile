package com.pile.features.search.di

import com.pile.features.search.ui.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel { params ->
        SearchViewModel(
            pileId = params.getOrNull(),
            requestBitmapLoadUseCase = get(),
            pileRepository = get(),
            documentRepository = get(),
            bitmapCacheRepository = get()
        )
    }
}
