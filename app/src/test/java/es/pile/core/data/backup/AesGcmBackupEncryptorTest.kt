package es.pile.core.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class AesGcmBackupEncryptorTest {

    private val encryptor = AesGcmBackupEncryptor()
    private val masterKey = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" // 32 bytes

    @Test
    fun `encrypt and decrypt should return original data`() {
        val originalData = "Hello, Pile Encryption!".toByteArray()
        val input = ByteArrayInputStream(originalData)

        // Encrypt
        val encryptedStream = encryptor.wrapForEncryption(input, masterKey)
        val encryptedData = encryptedStream.readBytes()

        assertNotEquals(originalData.toList(), encryptedData.toList())
        
        // Decrypt
        val decryptInput = ByteArrayInputStream(encryptedData)
        val decryptedStream = encryptor.wrapForDecryption(decryptInput, masterKey)
        val decryptedData = decryptedStream.readBytes()

        assertEquals(originalData.toList(), decryptedData.toList())
    }

    @Test
    fun `decrypting plaintext should return same data (transparent mode)`() {
        val plaintext = "Just some random text without header".toByteArray()
        val input = ByteArrayInputStream(plaintext)

        val resultStream = encryptor.wrapForDecryption(input, masterKey)
        val resultData = resultStream.readBytes()

        assertEquals(plaintext.toList(), resultData.toList())
    }

    @Test(expected = Exception::class)
    fun `decrypting with wrong key should throw exception`() {
        val originalData = "secret".toByteArray()
        val encryptedData = encryptor.wrapForEncryption(ByteArrayInputStream(originalData), masterKey).readBytes()

        val wrongKey = "f" + masterKey.substring(1)
        val decryptedStream = encryptor.wrapForDecryption(ByteArrayInputStream(encryptedData), wrongKey)
        decryptedStream.readBytes() // This should trigger the authentication tag failure
    }
}
