package com.pile.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.pile.core.data.local.UserSettingsSerializer
import com.pile.core.data.repositories.BitmapCacheRepositoryImpl
import com.pile.core.data.repositories.DataStoreSettingsRepository
import com.pile.core.data.repositories.DocumentImageRepositoryImpl
import com.pile.core.data.repositories.DocumentModelRepositoryImpl
import com.pile.core.data.repositories.FileRepositoryImpl
import com.pile.core.data.repositories.PileModelRepositoryImpl
import com.pile.core.data.util.ImageTransformationHelper
import com.pile.core.data.util.PdfRenderHelper
import com.pile.core.domain.models.UserSettings
import com.pile.core.domain.repositories.BitmapCacheRepository
import com.pile.core.domain.repositories.DocumentImageRepository
import com.pile.core.domain.repositories.DocumentModelRepository
import com.pile.core.domain.repositories.FileRepository
import com.pile.core.domain.repositories.PileModelRepository
import com.pile.core.domain.repositories.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val Context.dataStore: DataStore<UserSettings> by dataStore(
    fileName = "settings.json",
    serializer = UserSettingsSerializer,
)

val dataModule = module {
    single {
        androidContext().dataStore
    }

    single<SettingsRepository> {
        DataStoreSettingsRepository(dataStore = get(), ioDispatcher = get())
    }

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