package es.pile.features.backup.di

import es.pile.core.domain.backup.BackupProvider
import es.pile.features.backup.data.NoOpBackupAuthHandler
import es.pile.features.backup.domain.BackupAuthHandler
import org.koin.dsl.module

val backupFlavorModule = module {
    single<List<BackupProvider>> {
        emptyList()
    }

    single<BackupAuthHandler> { NoOpBackupAuthHandler() }
}
