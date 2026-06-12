package es.pile.features.documentDetail.di

import es.pile.features.documentDetail.data.helper.DocumentOpenerImpl
import es.pile.features.documentDetail.domain.helper.DocumentOpener
import es.pile.features.documentDetail.domain.useCases.DeleteDocumentUseCase
import es.pile.features.documentDetail.domain.useCases.GetDocumentDetailDataUseCase
import es.pile.features.documentDetail.domain.useCases.ManageDocumentPileUseCase
import es.pile.features.documentDetail.domain.useCases.UpdateDocumentDetailsUseCase
import es.pile.features.documentDetail.domain.useCases.export.ExportDocumentUseCase
import es.pile.features.documentDetail.domain.useCases.export.GeneratePdfUseCase
import es.pile.features.documentDetail.domain.useCases.export.GetPdfUriUseCase
import es.pile.features.documentDetail.domain.useCases.export.GetUpToDatePdfUseCase
import es.pile.features.documentDetail.ui.DocumentDetailViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val documentDetailModule = module {
    single<DocumentOpener> { DocumentOpenerImpl(get()) }

    factoryOf(::UpdateDocumentDetailsUseCase)
    factoryOf(::DeleteDocumentUseCase)
    factoryOf(::GetDocumentDetailDataUseCase)
    factoryOf(::GeneratePdfUseCase)
    factoryOf(::GetPdfUriUseCase)
    factoryOf(::GetUpToDatePdfUseCase)
    factoryOf(::ExportDocumentUseCase)
    factoryOf(::ManageDocumentPileUseCase)

    viewModelOf(::DocumentDetailViewModel)
}