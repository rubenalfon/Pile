package com.pile.features.documentDetail.di

import com.pile.features.documentDetail.data.helper.DocumentOpenerImpl
import com.pile.features.documentDetail.domain.helper.DocumentOpener
import com.pile.features.documentDetail.domain.useCases.DeleteDocumentUseCase
import com.pile.features.documentDetail.domain.useCases.ManageDocumentPileUseCase
import com.pile.features.documentDetail.domain.useCases.UpdateDocumentDetailsUseCase
import com.pile.features.documentDetail.domain.useCases.export.ExportDocumentUseCase
import com.pile.features.documentDetail.domain.useCases.export.GeneratePdfUseCase
import com.pile.features.documentDetail.domain.useCases.export.GetPdfUriUseCase
import com.pile.features.documentDetail.domain.useCases.export.GetUpToDatePdfUseCase
import com.pile.features.documentDetail.ui.DocumentDetailViewModel
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