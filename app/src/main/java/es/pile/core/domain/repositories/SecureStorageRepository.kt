package es.pile.core.domain.repositories

/**
 * Interface for securely storing and retrieving sensitive information (secrets).
 */
interface SecureStorageRepository {
    /**
     * Persists a secret string associated with a given key.
     * The implementation should handle the encryption of the value.
     *
     * @param key The unique identifier for the secret.
     * @param value The secret string to store.
     */
    suspend fun saveSecret(key: String, value: String)

    /**
     * Retrieves a secret string associated with a given key.
     * The implementation should handle the decryption of the value.
     *
     * @param key The unique identifier for the secret.
     * @return The decrypted secret string, or null if it doesn't exist.
     */
    suspend fun getSecret(key: String): String?

    /**
     * Removes a secret from the storage.
     *
     * @param key The unique identifier for the secret to remove.
     */
    suspend fun removeSecret(key: String)
}
