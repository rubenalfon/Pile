package es.pile.features.backup.di

import es.pile.core.domain.backup.BackupProvider
import es.pile.features.backup.data.GoogleBackupAuthHandler
import es.pile.features.backup.data.GoogleDriveBackupProvider
import es.pile.features.backup.domain.BackupAuthHandler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val backupFlavorModule = module {
    single<List<BackupProvider>> {
        listOf(
            GoogleDriveBackupProvider(androidContext())
        )
    }

    single<BackupAuthHandler> { GoogleBackupAuthHandler() }
}
