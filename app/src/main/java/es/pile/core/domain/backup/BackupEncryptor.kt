package es.pile.core.domain.backup

import java.io.InputStream

/**
 * Interface defining the encryption/decryption layer for backups.
 */
interface BackupEncryptor {
    /**
     * Wraps an [InputStream] to provide encryption while reading.
     *
     * @param input The stream to encrypt.
     * @param masterKeyHex The master key to use for encryption, or null for no encryption.
     */
    fun wrapForEncryption(input: InputStream, masterKeyHex: String?): InputStream

    /**
     * Wraps an [InputStream] to provide decryption while reading.
     *
     * @param input The stream to decrypt.
     * @param masterKeyHex The master key to use for decryption, or null if only transparent mode is needed.
     */
    fun wrapForDecryption(input: InputStream, masterKeyHex: String?): InputStream
}

