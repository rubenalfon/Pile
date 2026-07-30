package es.pile.core.data.repositories

import es.pile.core.data.backup.InvalidEncryptionKeyException
import es.pile.core.data.backup.models.BackupDto
import es.pile.core.data.backup.models.DocumentModelDto
import es.pile.core.domain.backup.BackupEncryptor
import es.pile.core.domain.backup.BackupProvider
import es.pile.core.domain.backup.RemoteFile
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.repositories.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import javax.crypto.AEADBadTagException

class BackupRepositoryImplTest {

    private val documentModelRepository: DocumentModelRepository = mockk(relaxed = true)
    private val documentImageRepository: DocumentImageRepository = mockk(relaxed = true)
    private val pileModelRepository: PileModelRepository = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val backupEncryptor: BackupEncryptor = mockk()
    private val json = Json { ignoreUnknownKeys = true }
    private val provider: BackupProvider = mockk()

    private val repository = BackupRepositoryImpl(
        documentModelRepository,
        documentImageRepository,
        pileModelRepository,
        fileRepository,
        settingsRepository,
        backupEncryptor,
        json,
        Dispatchers.Unconfined,
        listOf(provider)
    )

    private fun createMockDocument(id: String, modificationDate: LocalDateTime): es.pile.DocumentModel {
        return mockk {
            every { this@mockk.id } returns id
            every { this@mockk.title } returns "Title"
            every { this@mockk.imageIds } returns emptyList()
            every { this@mockk.creationDateTime } returns modificationDate
            every { this@mockk.modificationDateTime } returns modificationDate
            every { this@mockk.documentStatus } returns 0
            every { this@mockk.documentPileIds } returns emptyList()
            every { this@mockk.documentDetails } returns emptyList()
            every { this@mockk.documentNote } returns ""
            every { this@mockk.documentOrganizationIds } returns emptyList()
            every { this@mockk.isIncomingPdf } returns false
        }
    }

    @Test
    fun `when sync is called, then it should upload metadata and files`() = runTest {
        // Given
        val settings = UserSettings(isBackupEncryptionEnabled = false)
        every { settingsRepository.userSettings } returns flowOf(settings)
        coEvery { settingsRepository.getBackupMasterKey() } returns null
        coEvery { documentModelRepository.getAllDocumentModels() } returns emptyList()
        coEvery { documentImageRepository.getAllDocumentImages() } returns emptyList()
        coEvery { pileModelRepository.getAllPileModels() } returns emptyList()
        every { backupEncryptor.wrapForEncryption(any(), any()) } answers { it.invocation.args[0] as InputStream }
        coEvery { provider.uploadFile(any(), any(), any()) } returns Result.success("id")
        coEvery { provider.listFiles() } returns Result.success(emptyList())

        // When
        val result = repository.sync(provider)

        // Then
        assertTrue("Sync should be successful", result.isSuccess)
        coVerify { provider.uploadFile("backup_metadata.json", any(), any()) }
    }

    @Test
    fun `when sync is called with encryption, then it should wrap content with master key`() = runTest {
        // Given
        val masterKey = "secure-key"
        val settings = UserSettings(isBackupEncryptionEnabled = true)
        every { settingsRepository.userSettings } returns flowOf(settings)
        coEvery { settingsRepository.getBackupMasterKey() } returns masterKey
        coEvery { documentModelRepository.getAllDocumentModels() } returns emptyList()
        coEvery { documentImageRepository.getAllDocumentImages() } returns emptyList()
        coEvery { pileModelRepository.getAllPileModels() } returns emptyList()
        coEvery { provider.listFiles() } returns Result.success(emptyList())
        
        every { backupEncryptor.wrapForEncryption(any(), masterKey) } returns ByteArrayInputStream(byteArrayOf())
        coEvery { provider.uploadFile(any(), any(), any()) } returns Result.success("id")

        // When
        val result = repository.sync(provider)

        // Then
        assertTrue("Sync should be successful", result.isSuccess)
        coVerify { backupEncryptor.wrapForEncryption(any(), masterKey) }
    }

    @Test
    fun `when sync finds remote metadata, then it should download and decrypt it`() = runTest {
        // Given
        val settings = UserSettings(isBackupEncryptionEnabled = false)
        val remoteMetadata = BackupDto(timestamp = "2024-01-01T00:00:00", documents = emptyList(), images = emptyList(), piles = emptyList())
        val metadataJson = json.encodeToString(BackupDto.serializer(), remoteMetadata)
        
        every { settingsRepository.userSettings } returns flowOf(settings)
        coEvery { provider.listFiles() } returns Result.success(listOf(RemoteFile("m1", "backup_metadata.json")))
        coEvery { provider.downloadFile("m1") } returns Result.success(ByteArrayInputStream(metadataJson.toByteArray()))
        every { backupEncryptor.wrapForDecryption(any(), any()) } answers { it.invocation.args[0] as InputStream }
        
        coEvery { documentModelRepository.getAllDocumentModels() } returns emptyList()
        coEvery { provider.uploadFile(any(), any(), any()) } returns Result.success("id")
        every { backupEncryptor.wrapForEncryption(any(), any()) } answers { it.invocation.args[0] as InputStream }

        // When
        val result = repository.sync(provider)

        // Then
        assertTrue("Sync should be successful. Error: ${result.exceptionOrNull()?.message}", result.isSuccess)
        coVerify { provider.downloadFile("m1") }
        coVerify { backupEncryptor.wrapForDecryption(any(), any()) }
    }

    @Test
    fun `when decryption fails with BadTag, then it should throw InvalidEncryptionKeyException`() = runTest {
        // Given
        val settings = UserSettings(isBackupEncryptionEnabled = true)
        every { settingsRepository.userSettings } returns flowOf(settings)
        coEvery { settingsRepository.getBackupMasterKey() } returns "wrong-key"
        coEvery { provider.listFiles() } returns Result.success(listOf(RemoteFile("m1", "backup_metadata.json")))
        coEvery { provider.downloadFile("m1") } returns Result.success(ByteArrayInputStream(byteArrayOf()))
        
        val failingStream = object : InputStream() {
            override fun read(): Int = throw AEADBadTagException("Bad Tag")
            override fun read(b: ByteArray?, off: Int, len: Int): Int = throw AEADBadTagException("Bad Tag")
        }
        every { backupEncryptor.wrapForDecryption(any(), "wrong-key") } returns failingStream

        // When
        val result = repository.sync(provider)

        // Then
        assertTrue("Sync should fail", result.isFailure)
        assertTrue("Exception should be InvalidEncryptionKeyException", result.exceptionOrNull() is InvalidEncryptionKeyException)
    }

    @Test
    fun `when getSyncStatus is called and metadata exists, then it should return status with formatted timestamp`() = runTest {
        // Given
        val timestamp = 1722687801000L // 2024-08-03
        val remoteFile = RemoteFile(
            id = "id",
            name = "backup_metadata.json",
            lastModified = timestamp
        )
        coEvery { provider.listFiles() } returns Result.success(listOf(remoteFile))
        coEvery { documentModelRepository.getAllDocumentModels() } returns emptyList()

        // When
        val result = repository.getSyncStatus(provider)

        // Then
        assertTrue(result.isSuccess)
        val status = result.getOrThrow()
        assertTrue(status.lastBackupDateTime != null)
        assertTrue(status.lastBackupDateTime!!.contains("2024-08-03"))
    }

    @Test
    fun `when getSyncStatus is called and local files are missing on remote, then it should return correct count`() = runTest {
        // Given
        val localDoc = createMockDocument("doc1", LocalDateTime.now())
        coEvery { documentModelRepository.getAllDocumentModels() } returns listOf(localDoc)
        
        val pdfFile = File("doc1.pdf")
        val imgFile = File("img1")
        every { fileRepository.getPDFFile(documentId = "doc1") } returns pdfFile
        every { fileRepository.getImageFile(documentId = "doc1", imageId = "img1") } returns imgFile
        
        // Remote has nothing
        coEvery { provider.listFiles() } returns Result.success(emptyList())

        // When
        val result = repository.getSyncStatus(provider)

        // Then
        assertTrue(result.isSuccess)
        val status = result.getOrThrow()
        assertEquals(1, status.missingLocalFilesCount) // 1 PDF (mock imageIds was empty)
    }

    @Test
    fun `when sync is called and remote document is newer, then it should update local repository`() = runTest {
        // Given
        val localDoc = createMockDocument("doc1", LocalDateTime.of(2023, 1, 1, 0, 0))
        
        val remoteDoc = DocumentModelDto(
            id = "doc1", title = "New Title", imageIds = emptyList(),
            creationDateTime = "2024-01-01T12:00:00",
            modificationDateTime = "2024-01-01T12:00:00",
            documentStatus = 0, documentPileIds = emptyList(),
            documentDetails = emptyList(), documentNote = "",
            documentOrganizationIds = emptyList(), isIncomingPdf = false
        )
        val remoteMetadata = BackupDto(
            timestamp = "2024-01-01T12:00:00",
            documents = listOf(remoteDoc),
            images = emptyList(),
            piles = emptyList()
        )
        val metadataJson = json.encodeToString(BackupDto.serializer(), remoteMetadata)

        every { settingsRepository.userSettings } returns flowOf(UserSettings(isBackupEncryptionEnabled = false))
        coEvery { provider.listFiles() } returns Result.success(listOf(RemoteFile("m1", "backup_metadata.json")))
        coEvery { provider.downloadFile("m1") } returns Result.success(ByteArrayInputStream(metadataJson.toByteArray()))
        every { backupEncryptor.wrapForDecryption(any(), any()) } answers { it.invocation.args[0] as InputStream }
        
        coEvery { documentModelRepository.getAllDocumentModels() } returns listOf(localDoc)
        coEvery { provider.uploadFile(any(), any(), any()) } returns Result.success("id")
        every { backupEncryptor.wrapForEncryption(any(), any()) } answers { it.invocation.args[0] as InputStream }

        // When
        val result = repository.sync(provider)

        // Then
        assertTrue("Sync should be successful. Error: ${result.exceptionOrNull()?.message}", result.isSuccess)
        coVerify { documentModelRepository.insertDocumentModel(any()) }
    }
}
