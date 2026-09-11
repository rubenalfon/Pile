package es.pile.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import es.pile.core.data.local.AppPreferencesSerializer
import es.pile.core.data.local.UserSettingsSerializer
import es.pile.core.data.repositories.AndroidSecureStorageRepository
import es.pile.core.data.repositories.BitmapCacheRepositoryImpl
import es.pile.core.data.repositories.DataStoreAppPreferencesRepository
import es.pile.core.data.repositories.DataStoreSettingsRepository
import es.pile.core.data.repositories.DeletedEntityRepositoryImpl
import es.pile.core.data.repositories.DocumentImageRepositoryImpl
import es.pile.core.data.repositories.DocumentModelRepositoryImpl
import es.pile.core.data.repositories.FileRepositoryImpl
import es.pile.core.data.repositories.PileModelRepositoryImpl
import es.pile.core.data.sync.SyncManagerImpl
import es.pile.core.data.util.CryptographyManager
import es.pile.core.data.util.CryptographyManagerImpl
import es.pile.core.data.util.ImageTransformationHelper
import es.pile.core.data.util.PdfRenderHelper
import es.pile.core.domain.models.AppPreferences
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.AppPreferencesRepository
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DeletedEntityRepository
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.repositories.SecureStorageRepository
import es.pile.core.domain.repositories.SettingsRepository
import es.pile.core.domain.sync.SyncManager
import es.pile.core.domain.usecases.sync.PerformSyncUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val Context.dataStore: DataStore<UserSettings> by dataStore(
    fileName = "settings.json",
    serializer = UserSettingsSerializer,
)

val Context.appPrefsDataStore: DataStore<AppPreferences> by dataStore(
    fileName = "app_preferences.json",
    serializer = AppPreferencesSerializer,
)

val dataModule = module {
    single<CryptographyManager> { CryptographyManagerImpl() }

    single<SecureStorageRepository> {
        AndroidSecureStorageRepository(
            context = androidContext(),
            cryptographyManager = get(),
            ioDispatcher = get()
        )
    }

    single<SettingsRepository> {
        DataStoreSettingsRepository(
            dataStore = androidContext().dataStore,
            secureStorageRepository = get(),
            ioDispatcher = get()
        )
    }

    single<AppPreferencesRepository> {
        DataStoreAppPreferencesRepository(dataStore = androidContext().appPrefsDataStore, ioDispatcher = get())
    }

    singleOf(::ImageTransformationHelper)
    singleOf(::PdfRenderHelper)

    single { PerformSyncUseCase(backupRepository = get(), settingsRepository = get()) }

    single<SyncManager> {
        SyncManagerImpl(
            context = androidContext(),
            documentModelRepository = get(),
            pileModelRepository = get(),
            settingsRepository = get(),
            performSyncUseCase = get(),
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        )
    }

    single<DeletedEntityRepository> {
        DeletedEntityRepositoryImpl(
            databaseQueries = get(),
            ioDispatcher = get()
        )
    }

    single<PileModelRepository> {
        PileModelRepositoryImpl(
            databaseQueries = get(),
            deletedEntityRepository = get(),
            ioDispatcher = get()
        )
    }

    single<DocumentModelRepository> {
        DocumentModelRepositoryImpl(
            databaseQueries = get(),
            deletedEntityRepository = get(),
            ioDispatcher = get()
        )
    }

    single<DocumentImageRepository> {
        DocumentImageRepositoryImpl(
            databaseQueries = get(),
            deletedEntityRepository = get(),
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