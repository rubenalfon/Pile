package es.pile.core.data.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Interface for managing encryption and decryption using the Android KeyStore.
 */
interface CryptographyManager {
    fun encrypt(plainText: String, alias: String): String
    fun decrypt(encryptedText: String, alias: String): String
}

class CryptographyManagerImpl : CryptographyManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun encrypt(plainText: String, alias: String): String {
        val cipher = getEncryptCipher(alias)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        val combined = cipher.iv + encryptedBytes // GCM IV size is 12 bytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    override fun decrypt(encryptedText: String, alias: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, 12) // GCM IV size is 12 bytes
        val encryptedBytes = combined.copyOfRange(12, combined.size)
        
        val cipher = getDecryptCipher(alias, iv)
        return String(cipher.doFinal(encryptedBytes))
    }

    private fun getEncryptCipher(alias: String): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(alias))
        return cipher
    }

    private fun getDecryptCipher(alias: String, iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(alias), spec)
        return cipher
    }

    private fun getOrCreateKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey(alias)
    }

    private fun createKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
