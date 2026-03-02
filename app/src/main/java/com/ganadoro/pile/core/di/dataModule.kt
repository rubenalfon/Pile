package com.ganadoro.pile.core.di

import com.ganadoro.pile.core.data.repositories.BitmapCacheRepositoryImpl
import com.ganadoro.pile.core.data.repositories.DocumentImageRepositoryImpl
import com.ganadoro.pile.core.data.repositories.DocumentModelRepositoryImpl
import com.ganadoro.pile.core.data.repositories.FileRepositoryImpl
import com.ganadoro.pile.core.data.repositories.PileModelRepositoryImpl
import com.ganadoro.pile.core.data.util.ImageTransformationHelper
import com.ganadoro.pile.core.data.util.PdfRenderHelper
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository
import com.ganadoro.pile.core.domain.repositories.PileModelRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::ImageTransformationHelper)
    singleOf(::PdfRenderHelper)

    single<PileModelRepository> {
        PileModelRepositoryImpl(
            databaseQueries = get(),
            ioDispatcher = get()
        )
    }

    single<DocumentModelRepository> {
        DocumentModelRepositoryImpl(
            databaseQueries = get(),
            ioDispatcher = get()
        )
    }

    single<DocumentImageRepository> {
        DocumentImageRepositoryImpl(
            databaseQueries = get(),
            ioDispatcher = get()
        )
    }

    single<FileRepository> {
        FileRepositoryImpl(
            appContext = get(),
            ioDispatcher = get(),
            pdfRenderHelper = get(),
            imageTransformationHelper = get()
        )
    }

    single<BitmapCacheRepository> {
        BitmapCacheRepositoryImpl(
            ioDispatcher = get(),
            pdfRenderHelper = get(),
            imageTransformationHelper = get()
        )
    }
}