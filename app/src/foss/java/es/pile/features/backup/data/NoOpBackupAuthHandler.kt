package es.pile.features.backup.data

import android.content.Context
import es.pile.features.backup.domain.BackupAuthHandler

class NoOpBackupAuthHandler : BackupAuthHandler {
    override suspend fun launchAccountPicker(context: Context, clientId: String): String? {
        return null
    }
}
