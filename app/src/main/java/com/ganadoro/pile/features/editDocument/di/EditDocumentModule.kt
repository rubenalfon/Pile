package com.ganadoro.pile.features.editDocument.di

import com.ganadoro.pile.features.editDocument.domain.useCases.AddPageToDocumentUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.GetCropControllerUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.RemoveBitmapFromCacheUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.RequestDraftBitmapLoadUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.RequestThumbnailLoadUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.UpdateDocumentUseCase
import com.ganadoro.pile.features.editDocument.ui.EditDocumentViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val editDocumentModule = module {
    factoryOf(::RequestDraftBitmapLoadUseCase)
    factoryOf(::RemoveBitmapFromCacheUseCase)
    factoryOf(::RequestThumbnailLoadUseCase)
    factoryOf(::AddPageToDocumentUseCase)
    factoryOf(::UpdateDocumentUseCase)
    factoryOf(::GetCropControllerUseCase)

    viewModelOf(::EditDocumentViewModel)
}