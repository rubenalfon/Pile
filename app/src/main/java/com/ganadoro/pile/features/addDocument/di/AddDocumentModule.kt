package com.ganadoro.pile.features.addDocument.di

import com.ganadoro.pile.features.addDocument.domain.useCases.SaveDocumentUseCase
import com.ganadoro.pile.features.addDocument.ui.AddDocumentViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val addDocumentModule = module {
    factoryOf(::SaveDocumentUseCase)


    viewModelOf(::AddDocumentViewModel)
}