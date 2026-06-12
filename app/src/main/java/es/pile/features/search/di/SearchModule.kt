package es.pile.features.search.di

import es.pile.features.search.domain.useCases.SearchDocumentsUseCase
import es.pile.features.search.ui.SearchViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    factoryOf(::SearchDocumentsUseCase)

    viewModel { params ->
        SearchViewModel(
            pileId = params.getOrNull(),
            requestBitmapLoadUseCase = get(),
            searchDocumentsUseCase = get(),
            pileRepository = get(),
            documentRepository = get(),
            bitmapCacheRepository = get()
        )
    }
}
