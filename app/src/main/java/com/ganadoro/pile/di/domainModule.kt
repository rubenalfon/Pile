package com.ganadoro.pile.di

import com.ganadoro.pile.domain.usecases.RemoveFromCacheUseCase
import com.ganadoro.pile.domain.usecases.RequestDraftBitmapLoadUseCase
import com.ganadoro.pile.domain.usecases.document.AddPageToDocumentUseCase
import com.ganadoro.pile.domain.usecases.document.CreateDocumentUseCase
import com.ganadoro.pile.domain.usecases.document.DeleteDocumentUseCase
import com.ganadoro.pile.domain.usecases.document.ManageTemporaryDocumentUseCase
import com.ganadoro.pile.domain.usecases.document.UpdateDocumentDetailsUseCase
import com.ganadoro.pile.domain.usecases.document.UpdateDocumentUseCase
import com.ganadoro.pile.domain.usecases.export.ExportDocumentUseCase
import com.ganadoro.pile.domain.usecases.export.GeneratePdfUseCase
import com.ganadoro.pile.domain.usecases.export.GetPdfUriUseCase
import com.ganadoro.pile.domain.usecases.export.GetUpToDatePdfUseCase
import com.ganadoro.pile.domain.usecases.image.GetAvailableFiltersUseCase
import com.ganadoro.pile.domain.usecases.image.GetCropControllerUseCase
import com.ganadoro.pile.domain.usecases.image.RequestBitmapLoadUseCase
import com.ganadoro.pile.domain.usecases.image.RequestThumbnailLoadUseCase
import com.ganadoro.pile.domain.usecases.pile.CreatePileUseCase
import com.ganadoro.pile.domain.usecases.pile.ManageDocumentPileUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::CreateDocumentUseCase)
    factoryOf(::ManageTemporaryDocumentUseCase)
    factoryOf(::CreatePileUseCase)
    factoryOf(::RequestBitmapLoadUseCase)
    factoryOf(::DeleteDocumentUseCase)
    factoryOf(::UpdateDocumentDetailsUseCase)
    factoryOf(::RemoveFromCacheUseCase)
    factoryOf(::GetAvailableFiltersUseCase)
    factoryOf(::UpdateDocumentUseCase)
    factoryOf(::AddPageToDocumentUseCase)
    factoryOf(::ManageDocumentPileUseCase)
    factoryOf(::GeneratePdfUseCase)
    factoryOf(::GetPdfUriUseCase)
    factoryOf(::GetUpToDatePdfUseCase)
    factoryOf(::ExportDocumentUseCase)
    factoryOf(::RequestThumbnailLoadUseCase)
    factoryOf(::GetCropControllerUseCase)
    factoryOf(::RequestDraftBitmapLoadUseCase)
}