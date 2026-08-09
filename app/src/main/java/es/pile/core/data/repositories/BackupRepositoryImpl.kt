package es.pile.core.data.repositories

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.data.backup.models.BackupDto
import es.pile.core.data.backup.models.DocumentImageDto
import es.pile.core.data.backup.models.DocumentModelDto
import es.pile.core.data.backup.models.PileModelDto
import es.pile.core.domain.backup.BackupEncryptor
import es.pile.core.domain.backup.BackupProvider
import es.pile.core.domain.backup.RemoteFile
import es.pile.core.domain.models.BackupSyncStatus
import es.pile.core.domain.models.SyncState
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BackupRepositoryImpl(
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val pileModelRepository: PileModelRepository,
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository,
    private val backupEncryptor: BackupEncryptor,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
    override val availableProviders: List<BackupProvider>
) : BackupRepository {

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override suspend fun sync(
        provider: BackupProvider,
        tempMasterKey: String?,
        onProgress: (SyncState) -> Unit
    ): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                onProgress(SyncState.Syncing)
                val settings = settingsRepository.userSettings.first()
                val masterKey = tempMasterKey ?: settingsRepository.getBackupMasterKey()

                val remoteFiles = provider.listFiles().getOrThrow()
                val metadataFile = remoteFiles.find { it.name == "backup_metadata.json" }


                // 1. Download Remote Metadata if exists
                val remoteBackupDto = if (metadataFile != null) {
                    onProgress(SyncState.Downloading)
                    val remoteInput = provider.downloadFile(metadataFile.id).getOrThrow()
                    val decryptedInput =
                        backupEncryptor.wrapForDecryption(remoteInput, masterKey)

                    val jsonString = try {
                        decryptedInput.bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        // Check if it's a decryption error (Bad Tag)
                        val cause = e.cause
                        if (cause is javax.crypto.AEADBadTagException || e is javax.crypto.AEADBadTagException) {
                            throw es.pile.core.data.backup.InvalidEncryptionKeyException()
                        }
                        throw e
                    }

                    json.decodeFromString<BackupDto>(jsonString)
                } else {
                    null
                }

                // 2. Resolve Conflicts for Documents, Piles, and Images

                // Sync Piles
                val localPiles = pileModelRepository.getAllPileModels()
                val localPileMap = localPiles.associateBy { it.id }
                val remotePileMap = remoteBackupDto?.piles?.associateBy { it.id } ?: emptyMap()
                (localPileMap.keys + remotePileMap.keys).forEach { id ->
                    val local = localPileMap[id]
                    val remoteDto = remotePileMap[id]
                    if (local != null && remoteDto != null) {
                        val remoteDate = LocalDateTime.parse(
                            remoteDto.modificationDateTime ?: remoteBackupDto?.timestamp,
                            dateTimeFormatter
                        )
                        if (remoteDate.isAfter(local.modificationDateTime)) {
                            pileModelRepository.insertPileModel(remoteDto.toDomain(remoteBackupDto?.timestamp))
                        }
                    } else if (remoteDto != null) {
                        pileModelRepository.insertPileModel(remoteDto.toDomain(remoteBackupDto?.timestamp))
                    }
                }

                // Sync Images metadata
                val localImages = documentImageRepository.getAllDocumentImages()
                val localImageMap = localImages.associateBy { it.id }
                val remoteImageMap = remoteBackupDto?.images?.associateBy { it.id } ?: emptyMap()
                (localImageMap.keys + remoteImageMap.keys).forEach { id ->
                    val local = localImageMap[id]
                    val remoteDto = remoteImageMap[id]
                    if (local != null && remoteDto != null) {
                        val remoteDate = LocalDateTime.parse(
                            remoteDto.modificationDateTime ?: remoteBackupDto?.timestamp,
                            dateTimeFormatter
                        )
                        if (remoteDate.isAfter(local.modificationDateTime)) {
                            documentImageRepository.insertDocumentImage(
                                remoteDto.toDomain(remoteBackupDto?.timestamp)
                            )
                        }
                    } else if (remoteDto != null) {
                        documentImageRepository.insertDocumentImage(
                            remoteDto.toDomain(
                                remoteBackupDto?.timestamp
                            )
                        )
                    }
                }

                // Sync Documents and their physical files
                val localDocuments = documentModelRepository.getAllDocumentModels()
                val localDocMap = localDocuments.associateBy { it.id }
                val remoteDocMap = remoteBackupDto?.documents?.associateBy { it.id } ?: emptyMap()

                val allDocIds = localDocMap.keys + remoteDocMap.keys
                for (docId in allDocIds) {
                    val localDoc = localDocMap[docId]
                    val remoteDocDto = remoteDocMap[docId]

                    if (localDoc != null && remoteDocDto != null) {
                        val remoteDate = LocalDateTime.parse(
                            remoteDocDto.modificationDateTime ?: remoteDocDto.creationDateTime,
                            dateTimeFormatter
                        )
                        if (remoteDate.isAfter(localDoc.modificationDateTime)) {
                            documentModelRepository.insertDocumentModel(remoteDocDto.toDomain())
                            downloadFilesForDoc(provider, remoteDocDto, remoteFiles, masterKey)
                        }
                    } else if (remoteDocDto != null) {
                        documentModelRepository.insertDocumentModel(remoteDocDto.toDomain())
                        downloadFilesForDoc(provider, remoteDocDto, remoteFiles, masterKey)
                    }
                }

                // 3. Prepare final upload (Consolidated data)
                onProgress(SyncState.Uploading)
                val updatedDocuments = documentModelRepository.getAllDocumentModels()
                val updatedImages = documentImageRepository.getAllDocumentImages()
                val updatedPiles = pileModelRepository.getAllPileModels()

                val currentRemoteFileNames =
                    provider.listFiles().getOrThrow().map { it.name }.toSet()
                val uploadEncryptionKey =
                    if (settings.isBackupEncryptionEnabled) masterKey else null

                for (doc in updatedDocuments) {
                    val pdfFile = fileRepository.getPDFFile(documentId = doc.id)
                    if (pdfFile.exists() && pdfFile.name !in currentRemoteFileNames) {
                        pdfFile.inputStream().use { input ->
                            provider.uploadFile(
                                pdfFile.name,
                                backupEncryptor.wrapForEncryption(input, uploadEncryptionKey),
                                mapOf("docId" to doc.id, "type" to "pdf")
                            ).getOrThrow()
                        }
                    }

                    for (imageId in doc.imageIds) {
                        val imageFile =
                            fileRepository.getImageFile(documentId = doc.id, imageId = imageId)
                        if (imageFile.exists() && imageFile.name !in currentRemoteFileNames) {
                            imageFile.inputStream().use { input ->
                                provider.uploadFile(
                                    imageFile.name,
                                    backupEncryptor.wrapForEncryption(input, uploadEncryptionKey),
                                    mapOf("docId" to doc.id, "type" to "image")
                                ).getOrThrow()
                            }
                        }
                    }
                }

                // 4. Upload final Metadata
                val backupDto = BackupDto(
                    timestamp = LocalDateTime.now().format(dateTimeFormatter),
                    documents = updatedDocuments.map { it.toDto() },
                    images = updatedImages.map { it.toDto() },
                    piles = updatedPiles.map { it.toDto() }
                )

                val jsonString = json.encodeToString(backupDto)
                val inputStream =
                    backupEncryptor.wrapForEncryption(
                        ByteArrayInputStream(jsonString.toByteArray()),
                        uploadEncryptionKey
                    )
                provider.uploadFile(
                    "backup_metadata.json",
                    inputStream,
                    mapOf("type" to "metadata")
                )
                    .getOrThrow()
                Unit
            }
        }

    private suspend fun downloadFilesForDoc(
        provider: BackupProvider,
        docDto: DocumentModelDto,
        remoteFiles: List<RemoteFile>,
        masterKey: String?
    ) {
        // PDF
        val pdfName = "${docDto.id}.pdf"
        val remotePdf = remoteFiles.find { it.name == pdfName }
        if (remotePdf != null) {
            val localFile = fileRepository.getPDFFile(documentId = docDto.id)
            if (!localFile.exists()) {
                localFile.parentFile?.mkdirs()
                provider.downloadFile(remotePdf.id).onSuccess { remoteInput ->
                    localFile.outputStream().use { output ->
                        backupEncryptor.wrapForDecryption(remoteInput, masterKey).copyTo(output)
                    }
                }
            }
        }

        // Images
        for (imageId in docDto.imageIds) {
            val remoteImage = remoteFiles.find { it.name == imageId }
            if (remoteImage != null) {
                val localFile =
                    fileRepository.getImageFile(documentId = docDto.id, imageId = imageId)
                if (!localFile.exists()) {
                    localFile.parentFile?.mkdirs()
                    provider.downloadFile(remoteImage.id).onSuccess { remoteInput ->
                        localFile.outputStream().use { output ->
                            backupEncryptor.wrapForDecryption(remoteInput, masterKey).copyTo(output)
                        }
                    }
                }
            }
        }
    }

    override suspend fun getSyncStatus(provider: BackupProvider): Result<BackupSyncStatus> =
        withContext(ioDispatcher) {
            runCatching {
                val remoteFiles = provider.listFiles().getOrThrow()
                val metadataFile = remoteFiles.find { it.name == "backup_metadata.json" }

                val lastBackupTime = metadataFile?.lastModified?.let { timestamp ->
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                        .format(dateTimeFormatter)
                }

                val localDocuments = documentModelRepository.getAllDocumentModels()
                val remoteFileNames = remoteFiles.map { it.name }.toSet()

                var missingCount = 0
                for (doc in localDocuments) {
                    if (fileRepository.getPDFFile(documentId = doc.id).name !in remoteFileNames) {
                        missingCount++
                    }
                    for (imageId in doc.imageIds) {
                        if (fileRepository.getImageFile(
                                documentId = doc.id,
                                imageId = imageId
                            ).name !in remoteFileNames
                        ) {
                            missingCount++
                        }
                    }
                }

                BackupSyncStatus(
                    lastBackupDateTime = lastBackupTime,
                    missingLocalFilesCount = missingCount,
                    totalRemoteFilesCount = remoteFiles.size
                )
            }
        }

    // Mappers
    private fun DocumentModel.toDto() = DocumentModelDto(
        id = id, title = title, imageIds = imageIds,
        creationDateTime = creationDateTime.format(dateTimeFormatter),
        modificationDateTime = modificationDateTime.format(dateTimeFormatter),
        documentStatus = documentStatus, documentPileIds = documentPileIds,
        documentDetails = documentDetails, documentNote = documentNote,
        documentOrganizationIds = documentOrganizationIds, isIncomingPdf = isIncomingPdf
    )

    private fun DocumentModelDto.toDomain() = DocumentModel(
        id = id, title = title, imageIds = imageIds,
        creationDateTime = LocalDateTime.parse(creationDateTime, dateTimeFormatter),
        modificationDateTime = LocalDateTime.parse(
            modificationDateTime ?: creationDateTime,
            dateTimeFormatter
        ),
        documentStatus = documentStatus, documentPileIds = documentPileIds,
        documentDetails = documentDetails, documentNote = documentNote,
        documentOrganizationIds = documentOrganizationIds, isIncomingPdf = isIncomingPdf
    )

    private fun DocumentImage.toDto() = DocumentImageDto(
        id = id, isDraft = isDraft, crop = crop,
        filter = filter.toInt(), rotation = rotation.toInt(),
        modificationDateTime = modificationDateTime.format(dateTimeFormatter)
    )

    private fun DocumentImageDto.toDomain(backupTimestamp: String? = null) = DocumentImage(
        id = id, isDraft = isDraft, crop = crop,
        filter = filter.toLong(), rotation = rotation.toLong(),
        modificationDateTime = LocalDateTime.parse(
            modificationDateTime ?: backupTimestamp ?: LocalDateTime.now()
                .format(dateTimeFormatter),
            dateTimeFormatter
        )
    )

    private fun PileModel.toDto() = PileModelDto(
        id = id, name = name, iconId = iconId, colorNumber = colorNumber,
        modificationDateTime = modificationDateTime.format(dateTimeFormatter)
    )

    private fun PileModelDto.toDomain(backupTimestamp: String? = null) = PileModel(
        id = id, name = name, iconId = iconId, colorNumber = colorNumber,
        modificationDateTime = LocalDateTime.parse(
            modificationDateTime ?: backupTimestamp ?: LocalDateTime.now()
                .format(dateTimeFormatter),
            dateTimeFormatter
        )
    )
}
