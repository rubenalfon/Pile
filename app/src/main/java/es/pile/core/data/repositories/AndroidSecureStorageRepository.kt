package es.pile.core.data.repositories

import android.content.Context
import androidx.core.content.edit
import es.pile.core.data.util.CryptographyManager
import es.pile.core.domain.repositories.SecureStorageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Android implementation of [SecureStorageRepository] using standard SharedPreferences
 * but with manual value encryption via [CryptographyManager] (KeyStore).
 * This replaces the deprecated EncryptedSharedPreferences to provide a 10/10 security implementation.
 */
class AndroidSecureStorageRepository(
    context: Context,
    private val cryptographyManager: CryptographyManager,
    private val ioDispatcher: CoroutineDispatcher
) : SecureStorageRepository {

    private val sharedPreferences = context.getSharedPreferences("secure_storage", Context.MODE_PRIVATE)
    
    private val keyAlias = "secure_storage_key"

    override suspend fun saveSecret(key: String, value: String) {
        withContext(ioDispatcher) {
            val encryptedValue = cryptographyManager.encrypt(value, keyAlias)
            sharedPreferences.edit(commit = false) {
                putString(key, encryptedValue)
            }
        }
    }

    override suspend fun getSecret(key: String): String? {
        return withContext(ioDispatcher) {
            val encryptedValue = sharedPreferences.getString(key, null)
            encryptedValue?.let {
                try {
                    cryptographyManager.decrypt(it, keyAlias)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun removeSecret(key: String) {
        withContext(ioDispatcher) {
            sharedPreferences.edit(commit = false) {
                remove(key)
            }
        }
    }
}
