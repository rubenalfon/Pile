package es.pile.core.domain.usecases.backup

import android.content.Context
import android.net.Uri
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
import java.io.BufferedOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportLocalBackupUseCase(
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
            val documents = documentModelRepository.getAllDocumentModels()
            val images = documentImageRepository.getAllDocumentImages()
            val piles = pileModelRepository.getAllPileModels()

            val backupDto = BackupDto(
                timestamp = LocalDateTime.now().format(dateTimeFormatter),
                documents = documents.map { doc ->
                    DocumentModelDto(
                        id = doc.id,
                        title = doc.title,
                        imageIds = doc.imageIds,
                        creationDateTime = doc.creationDateTime.format(dateTimeFormatter),
                        modificationDateTime = doc.modificationDateTime.format(dateTimeFormatter),
                        documentStatus = doc.documentStatus,
                        documentPileIds = doc.documentPileIds,
                        documentDetails = doc.documentDetails,
                        documentNote = doc.documentNote,
                        documentOrganizationIds = doc.documentOrganizationIds,
                        isIncomingPdf = doc.isIncomingPdf
                    )
                },
                images = images.map { img ->
                    DocumentImageDto(
                        id = img.id,
                        isDraft = img.isDraft,
                        crop = img.crop,
                        filter = img.filter.toInt(),
                        rotation = img.rotation.toInt(),
                        modificationDateTime = img.modificationDateTime.format(dateTimeFormatter)
                    )
                },
                piles = piles.map { pile ->
                    PileModelDto(
                        id = pile.id,
                        name = pile.name,
                        iconId = pile.iconId,
                        colorNumber = pile.colorNumber,
                        modificationDateTime = pile.modificationDateTime.format(dateTimeFormatter)
                    )
                }
            )

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // Write metadata
                    val metadataEntry = ZipEntry("backup_metadata.json")
                    zipOut.putNextEntry(metadataEntry)
                    val jsonString = json.encodeToString(backupDto)
                    zipOut.write(jsonString.toByteArray())
                    zipOut.closeEntry()

                    // Write files
                    val addedFileNames = mutableSetOf<String>()
                    
                    for (doc in documents) {
                        if (doc.isIncomingPdf) {
                            val pdfFile = fileRepository.getPDFFile(FileRepository.StorageType.PERSISTENT, doc.id)
                            if (pdfFile.exists() && addedFileNames.add(pdfFile.name)) {
                                zipOut.putNextEntry(ZipEntry(pdfFile.name))
                                pdfFile.inputStream().use { it.copyTo(zipOut) }
                                zipOut.closeEntry()
                            }
                        }

                        for (imageId in doc.imageIds) {
                            val imageFile = fileRepository.getImageFile(FileRepository.StorageType.PERSISTENT, doc.id, imageId)
                            if (imageFile.exists() && addedFileNames.add(imageFile.name)) {
                                zipOut.putNextEntry(ZipEntry(imageFile.name))
                                imageFile.inputStream().use { it.copyTo(zipOut) }
                                zipOut.closeEntry()
                            }
                        }
                    }
                }
            } ?: throw IllegalStateException("Could not open output stream")
        }
    }
}
