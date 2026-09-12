package es.pile.core.domain.usecases.backup

import android.content.Context
import android.net.Uri
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.data.backup.models.BackupDto
import es.pile.core.data.backup.models.DocumentImageDto
import es.pile.core.data.backup.models.DocumentModelDto
import es.pile.core.data.backup.models.PileModelDto
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

class ImportLocalBackupUseCase(
    private val context: Context,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val pileModelRepository: PileModelRepository,
    private val fileRepository: FileRepository,
    private val json: Json
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend operator fun invoke(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val tempDir = File(context.cacheDir, "temp_backup_restore")
            tempDir.mkdirs()
            tempDir.deleteRecursively() // Ensure it's clean
            tempDir.mkdirs()

            try {
                // 1. Extract ZIP to temp folder
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry = zipIn.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val outFile = File(tempDir, entry.name)
                                FileOutputStream(outFile).use { output ->
                                    zipIn.copyTo(output)
                                }
                            }
                            entry = zipIn.nextEntry
                        }
                    }
                } ?: throw IllegalStateException("Could not open input stream")

                // 2. Read metadata
                val metadataFile = File(tempDir, "backup_metadata.json")
                if (!metadataFile.exists()) {
                    throw IllegalStateException("Backup metadata not found in the ZIP.")
                }
                
                val jsonString = metadataFile.readText()
                val backupDto = json.decodeFromString<BackupDto>(jsonString)

                // 3. Insert/merge restored data (without deleting existing non-conflicting local data)
                backupDto.piles.forEach { dto ->
                    pileModelRepository.insertPileModel(dto.toDomain(backupDto.timestamp))
                }

                backupDto.images.forEach { dto ->
                    documentImageRepository.insertDocumentImage(dto.toDomain(backupDto.timestamp))
                }

                backupDto.documents.forEach { dto ->
                    documentModelRepository.insertDocumentModel(dto.toDomain())
                    
                    // Restore PDF
                    if (dto.isIncomingPdf) {
                        val tempPdfFile = File(tempDir, "${dto.id}.pdf")
                        if (tempPdfFile.exists()) {
                            val targetPdfFile = fileRepository.getPDFFile(documentId = dto.id)
                            targetPdfFile.parentFile?.mkdirs()
                            tempPdfFile.copyTo(targetPdfFile, overwrite = true)
                        }
                    }

                    // Restore Images
                    dto.imageIds.forEach { imageId ->
                        // The format of getImageFile is "img_$imageId.jpg", but the name in ZIP might just be that
                        val expectedName = fileRepository.getImageFile(documentId = dto.id, imageId = imageId).name
                        val tempImgFile = File(tempDir, expectedName)
                        if (tempImgFile.exists()) {
                            val targetImgFile = fileRepository.getImageFile(documentId = dto.id, imageId = imageId)
                            targetImgFile.parentFile?.mkdirs()
                            tempImgFile.copyTo(targetImgFile, overwrite = true)
                        }
                    }
                }
            } finally {
                // Cleanup temp dir
                tempDir.deleteRecursively()
            }
        }
    }

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

    private fun DocumentImageDto.toDomain(backupTimestamp: String? = null) = DocumentImage(
        id = id, isDraft = isDraft, crop = crop,
        filter = filter.toLong(), rotation = rotation.toLong(),
        modificationDateTime = LocalDateTime.parse(
            modificationDateTime ?: backupTimestamp ?: LocalDateTime.now()
                .format(dateTimeFormatter),
            dateTimeFormatter
        )
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
