package com.ganadoro.pile.features.documentDetail.di

import com.ganadoro.pile.features.documentDetail.data.helper.DocumentOpenerImpl
import com.ganadoro.pile.features.documentDetail.domain.helper.DocumentOpener
import com.ganadoro.pile.features.documentDetail.domain.useCases.DeleteDocumentUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.ManageDocumentPileUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.UpdateDocumentDetailsUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.export.ExportDocumentUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.export.GeneratePdfUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.export.GetPdfUriUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.export.GetUpToDatePdfUseCase
import com.ganadoro.pile.features.documentDetail.ui.DocumentDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val documentDetailModule = module {
    single<DocumentOpener> { DocumentOpenerImpl(get()) }

    factoryOf(::UpdateDocumentDetailsUseCase)
    factoryOf(::DeleteDocumentUseCase)
    factoryOf(::GeneratePdfUseCase)
    factoryOf(::GetPdfUriUseCase)
    factoryOf(::GetUpToDatePdfUseCase)
    factoryOf(::ExportDocumentUseCase)
    factoryOf(::ManageDocumentPileUseCase)

    viewModelOf(::DocumentDetailViewModel)
}