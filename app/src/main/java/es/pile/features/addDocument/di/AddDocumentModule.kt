package es.pile.features.addDocument.di

import es.pile.features.addDocument.domain.useCases.SaveDocumentUseCase
import es.pile.features.addDocument.ui.AddDocumentViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val addDocumentModule = module {
    factoryOf(::SaveDocumentUseCase)


    viewModelOf(::AddDocumentViewModel)
}