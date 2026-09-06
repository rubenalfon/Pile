package es.pile.features.backup.di

import es.pile.core.data.backup.AesGcmBackupEncryptor
import es.pile.core.data.repositories.BackupRepositoryImpl
import es.pile.core.domain.backup.BackupEncryptor
import es.pile.core.domain.repositories.BackupRepository
import es.pile.features.backup.ui.BackupViewModel
import es.pile.features.backup.ui.encryption.EncryptionViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val backupModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    single<BackupEncryptor> { AesGcmBackupEncryptor() }

    single<BackupRepository> {
        BackupRepositoryImpl(
            documentModelRepository = get(),
            documentImageRepository = get(),
            pileModelRepository = get(),
            deletedEntityRepository = get(),
            fileRepository = get(),
            settingsRepository = get(),
            backupEncryptor = get(),
            json = get(),
            ioDispatcher = get(),
            availableProviders = get()
        )
    }

    viewModelOf(::BackupViewModel)
    viewModelOf(::EncryptionViewModel)
}

