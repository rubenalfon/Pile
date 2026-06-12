package es.pile.features.editDocument.di

import es.pile.features.editDocument.data.helper.CropControllerFactoryImpl
import es.pile.features.editDocument.domain.helper.CropControllerFactory
import es.pile.features.editDocument.domain.useCases.AddPageToDocumentUseCase
import es.pile.features.editDocument.domain.useCases.FinalizeDocumentUpdateUseCase
import es.pile.features.editDocument.domain.useCases.GetCropControllerUseCase
import es.pile.features.editDocument.domain.useCases.RemoveBitmapFromCacheUseCase
import es.pile.features.editDocument.domain.useCases.RequestDraftBitmapLoadUseCase
import es.pile.features.editDocument.domain.useCases.RequestThumbnailLoadUseCase
import es.pile.features.editDocument.ui.EditDocumentViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val editDocumentModule = module {
    factoryOf(::RequestDraftBitmapLoadUseCase)
    factoryOf(::RemoveBitmapFromCacheUseCase)
    factoryOf(::RequestThumbnailLoadUseCase)
    factoryOf(::AddPageToDocumentUseCase)
    factoryOf(::FinalizeDocumentUpdateUseCase)
    factoryOf(::GetCropControllerUseCase)

    factory<CropControllerFactory> { CropControllerFactoryImpl() }

    viewModelOf(::EditDocumentViewModel)
}
