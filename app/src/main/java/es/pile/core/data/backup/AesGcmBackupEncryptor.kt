package es.pile.core.data.backup

import es.pile.core.domain.backup.BackupEncryptor
import java.io.InputStream
import java.io.SequenceInputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Real implementation of [BackupEncryptor] using AES-GCM-256 and HKDF for key derivation.
 * Supports a transparent mode: if the input doesn't start with "PILE", it's treated as plaintext.
 */
class AesGcmBackupEncryptor : BackupEncryptor {

    companion object {
        private const val MAGIC_BYTES = "PILE"
        private const val VERSION: Byte = 0x01
        private const val SALT_SIZE = 16
        private const val NONCE_SIZE = 12 // Standard for GCM
        private const val TAG_SIZE = 128

        /**
         * The header size is composed of:
         * - 4 bytes: "PILE" (Magic bytes used to identify encrypted files)
         * - 1 byte: Format version
         * - 16 bytes: Salt (used for key derivation)
         * - 12 bytes: Nonce (Initialization Vector for GCM)
         */
        private const val HEADER_SIZE = 4 + 1 + SALT_SIZE + NONCE_SIZE

        private const val ALGORITHM = "AES/GCM/NoPadding"
    }

    override fun wrapForEncryption(input: InputStream, masterKeyHex: String?): InputStream {
        if (masterKeyHex == null) return input

        val masterKey = masterKeyHex.decodeHex()
        val salt = ByteArray(SALT_SIZE).apply { SecureRandom().nextBytes(this) }
        val nonce = ByteArray(NONCE_SIZE).apply { SecureRandom().nextBytes(this) }

        val fileKey = deriveKey(masterKey, salt)
        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(fileKey, "AES"), GCMParameterSpec(TAG_SIZE, nonce))
        }

        val header = ByteBuffer.allocate(HEADER_SIZE)
            .put(MAGIC_BYTES.toByteArray())
            .put(VERSION)
            .put(salt)
            .put(nonce)
            .array()

        return SequenceInputStream(header.inputStream(), CipherInputStream(input, cipher))
    }

    override fun wrapForDecryption(input: InputStream, masterKeyHex: String?): InputStream {
        val bufferedInput = input.buffered()
        bufferedInput.mark(HEADER_SIZE)

        val header = ByteArray(4)
        val read = bufferedInput.read(header)

        if (read < 4 || String(header) != MAGIC_BYTES) {
            bufferedInput.reset()
            return bufferedInput
        }

        // It's a PILE encrypted file
        if (masterKeyHex == null) throw EncryptionKeyRequiredException()

        val version = bufferedInput.read()
        if (version != VERSION.toInt()) {
             bufferedInput.reset()
             return bufferedInput
        }

        val salt = ByteArray(SALT_SIZE).apply { bufferedInput.read(this) }
        val nonce = ByteArray(NONCE_SIZE).apply { bufferedInput.read(this) }

        val masterKey = masterKeyHex.decodeHex()
        val fileKey = deriveKey(masterKey, salt)

        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(fileKey, "AES"), GCMParameterSpec(TAG_SIZE, nonce))
        }

        return CipherInputStream(bufferedInput, cipher)
    }

    /**
     * Simple HKDF-Expand implementation using HMAC-SHA256.
     * info is fixed to "pile_backup_file"
     */
    private fun deriveKey(masterKey: ByteArray, salt: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = mac.doFinal(masterKey)

        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        mac.update("pile_backup_file".toByteArray())
        mac.update(0x01.toByte())
        return mac.doFinal().sliceArray(0 until 32)
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}

class EncryptionKeyRequiredException : Exception("Master key required to decrypt backup")

class InvalidEncryptionKeyException : Exception("The provided recovery key is incorrect or data is corrupted")
