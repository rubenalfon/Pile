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
import es.pile.core.domain.models.DeletedEntityType
import es.pile.core.domain.models.SyncState
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DeletedEntityRepository
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BackupRepositoryImpl(
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val pileModelRepository: PileModelRepository,
    private val deletedEntityRepository: DeletedEntityRepository,
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository,
    private val backupEncryptor: BackupEncryptor,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
    override val availableProviders: List<BackupProvider>,
    private val bitmapCacheRepository: BitmapCacheRepository? = null
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

                // 2. Consolidate Local and Remote Tombstones (Deleted Entities)
                val localDeletedDocIds =
                    deletedEntityRepository.getDeletedEntityIdsByType(DeletedEntityType.DOCUMENT)
                val localDeletedPileIds =
                    deletedEntityRepository.getDeletedEntityIdsByType(DeletedEntityType.PILE)
                val localDeletedImageIds =
                    deletedEntityRepository.getDeletedEntityIdsByType(DeletedEntityType.IMAGE)

                val remoteDeletedDocIds = remoteBackupDto?.deletedDocumentIds?.toSet() ?: emptySet()
                val remoteDeletedPileIds = remoteBackupDto?.deletedPileIds?.toSet() ?: emptySet()
                val remoteDeletedImageIds = remoteBackupDto?.deletedImageIds?.toSet() ?: emptySet()

                val allDeletedDocIds = localDeletedDocIds + remoteDeletedDocIds
                val allDeletedPileIds = localDeletedPileIds + remoteDeletedPileIds
                val allDeletedImageIds = localDeletedImageIds + remoteDeletedImageIds

                // Persist remote tombstones locally so local DB is aware of deletions on other devices
                (remoteDeletedDocIds - localDeletedDocIds).forEach {
                    deletedEntityRepository.insertDeletedEntity(it, DeletedEntityType.DOCUMENT)
                }
                (remoteDeletedPileIds - localDeletedPileIds).forEach {
                    deletedEntityRepository.insertDeletedEntity(it, DeletedEntityType.PILE)
                }
                (remoteDeletedImageIds - localDeletedImageIds).forEach {
                    deletedEntityRepository.insertDeletedEntity(it, DeletedEntityType.IMAGE)
                }

                // 3. Resolve Conflicts for Piles, Images, and Documents
                val modifiedCacheIds = mutableSetOf<String>()

                // 3.1 Sync Piles
                val localPiles = pileModelRepository.getAllPileModels()
                val localPileMap = localPiles.associateBy { it.id }
                val remotePileMap = remoteBackupDto?.piles?.associateBy { it.id } ?: emptyMap()

                for (id in allDeletedPileIds) {
                    if (id in localPileMap) pileModelRepository.deletePileModel(id)
                }

                for ((id, remoteDto) in remotePileMap) {
                    if (id in allDeletedPileIds) continue

                    val local = localPileMap[id]
                    if (local == null) {
                        pileModelRepository.insertPileModel(remoteDto.toDomain(remoteBackupDto?.timestamp))
                    } else {
                        val remoteDate = LocalDateTime.parse(
                            remoteDto.modificationDateTime ?: remoteBackupDto?.timestamp,
                            dateTimeFormatter
                        )
                        if (remoteDate.isAfter(local.modificationDateTime)) {
                            pileModelRepository.insertPileModel(remoteDto.toDomain(remoteBackupDto?.timestamp))
                        }
                    }
                }

                // 3.2 Sync Images metadata
                val localImages = documentImageRepository.getAllDocumentImages()
                val localImageMap = localImages.associateBy { it.id }
                val remoteImageMap = remoteBackupDto?.images?.associateBy { it.id } ?: emptyMap()

                for (id in allDeletedImageIds) {
                    if (id in localImageMap) {
                        documentImageRepository.deleteDocumentImage(id)
                        modifiedCacheIds.add(id)
                    }
                    remoteFiles.find { it.name == id }?.let { remoteImg ->
                        runCatching { provider.deleteFile(remoteImg.id) }
                    }
                }

                for ((id, remoteDto) in remoteImageMap) {
                    if (id in allDeletedImageIds) continue

                    val local = localImageMap[id]
                    if (local == null) {
                        documentImageRepository.insertDocumentImage(
                            remoteDto.toDomain(remoteBackupDto?.timestamp)
                        )
                        modifiedCacheIds.add(id)
                    } else {
                        val remoteDate = LocalDateTime.parse(
                            remoteDto.modificationDateTime ?: remoteBackupDto?.timestamp,
                            dateTimeFormatter
                        )
                        if (remoteDate.isAfter(local.modificationDateTime)) {
                            documentImageRepository.insertDocumentImage(
                                remoteDto.toDomain(remoteBackupDto?.timestamp)
                            )
                            modifiedCacheIds.add(id)
                        }
                    }
                }

                // 3.3 Sync Documents and their PDF files
                val localDocuments = documentModelRepository.getAllDocumentModels()
                val localDocMap = localDocuments.associateBy { it.id }
                val remoteDocMap = remoteBackupDto?.documents?.associateBy { it.id } ?: emptyMap()

                for (docId in allDeletedDocIds) {
                    if (docId in localDocMap) {
                        documentModelRepository.deleteDocumentModel(docId)
                        fileRepository.deleteDocumentStorage(documentId = docId)
                        modifiedCacheIds.add(docId)
                    }
                    val pdfName = "$docId.pdf"
                    remoteFiles.find { it.name == pdfName }?.let { remotePdf ->
                        runCatching { provider.deleteFile(remotePdf.id) }
                    }
                }

                for ((docId, remoteDocDto) in remoteDocMap) {
                    if (docId in allDeletedDocIds) continue

                    val localDoc = localDocMap[docId]
                    if (localDoc == null) {
                        documentModelRepository.insertDocumentModel(remoteDocDto.toDomain())
                        downloadFilesForDoc(provider, remoteDocDto, remoteFiles, masterKey)
                        modifiedCacheIds.add(docId)
                    } else {
                        val remoteDate = LocalDateTime.parse(
                            remoteDocDto.modificationDateTime ?: remoteDocDto.creationDateTime,
                            dateTimeFormatter
                        )
                        if (remoteDate.isAfter(localDoc.modificationDateTime)) {
                            documentModelRepository.insertDocumentModel(remoteDocDto.toDomain())
                            downloadFilesForDoc(provider, remoteDocDto, remoteFiles, masterKey)
                            modifiedCacheIds.add(docId)
                        }
                    }
                }

                // 4. Prepare final upload (Consolidated data excluding tombstones)
                onProgress(SyncState.Uploading)
                val updatedDocuments = documentModelRepository.getAllDocumentModels()
                    .filter { it.id !in allDeletedDocIds }
                val updatedImages = documentImageRepository.getAllDocumentImages()
                    .filter { it.id !in allDeletedImageIds }
                val updatedPiles = pileModelRepository.getAllPileModels()
                    .filter { it.id !in allDeletedPileIds }

                val currentRemoteFileNames =
                    provider.listFiles().getOrThrow().map { it.name }.toSet()
                val uploadEncryptionKey =
                    if (settings.isBackupEncryptionEnabled) masterKey else null

                for (doc in updatedDocuments) {
                    if (doc.isIncomingPdf) {
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
                    }

                    for (imageId in doc.imageIds) {
                        if (imageId in allDeletedImageIds) continue
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

                // 5. Upload final Metadata with Tombstones
                val backupDto = BackupDto(
                    timestamp = LocalDateTime.now().format(dateTimeFormatter),
                    documents = updatedDocuments.map { it.toDto() },
                    images = updatedImages.map { it.toDto() },
                    piles = updatedPiles.map { it.toDto() },
                    deletedDocumentIds = allDeletedDocIds.toList(),
                    deletedPileIds = allDeletedPileIds.toList(),
                    deletedImageIds = allDeletedImageIds.toList()
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

                bitmapCacheRepository?.invalidateCacheFor(modifiedCacheIds)
                Unit
            }
        }

    private suspend fun downloadFilesForDoc(
        provider: BackupProvider,
        docDto: DocumentModelDto,
        remoteFiles: List<RemoteFile>,
        masterKey: String?
    ) {
        if (docDto.isIncomingPdf) {
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
