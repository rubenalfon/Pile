package com.ganadoro.pile.di

import com.ganadoro.pile.data.WorkManagerCleanupScheduler
import com.ganadoro.pile.data.repositories.BitmapCacheRepositoryImpl
import com.ganadoro.pile.data.repositories.DocumentImageRepositoryImpl
import com.ganadoro.pile.data.repositories.DocumentModelRepositoryImpl
import com.ganadoro.pile.data.repositories.FileRepositoryImpl
import com.ganadoro.pile.data.repositories.PileModelRepositoryImpl
import com.ganadoro.pile.data.util.ImageTransformationHelper
import com.ganadoro.pile.data.util.PdfRenderHelper
import com.ganadoro.pile.domain.CleanupScheduler
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.repositories.FileRepository
import com.ganadoro.pile.domain.repositories.PileModelRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repositoriesModule = module {
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

    single<CleanupScheduler> {
        WorkManagerCleanupScheduler(get())
    }
}