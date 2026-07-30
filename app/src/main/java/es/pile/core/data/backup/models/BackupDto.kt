package es.pile.core.data.backup.models

import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.models.ImageCropData
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for the entire database backup.
 */
@Serializable
data class BackupDto(
    val version: Int = 1,
    val timestamp: String,
    val documents: List<DocumentModelDto>,
    val images: List<DocumentImageDto>,
    val piles: List<PileModelDto>
)

@Serializable
data class DocumentModelDto(
    val id: String,
    val title: String,
    val imageIds: List<String>,
    val creationDateTime: String,
    val modificationDateTime: String? = null,
    val documentStatus: Int,
    val documentPileIds: List<String>,
    val documentDetails: List<DocumentDetail>,
    val documentNote: String,
    val documentOrganizationIds: List<String>,
    val isIncomingPdf: Boolean
)

@Serializable
data class DocumentImageDto(
    val id: String,
    val isDraft: Boolean,
    val crop: ImageCropData?,
    val filter: Int,
    val rotation: Int,
    val modificationDateTime: String? = null
)

@Serializable
data class PileModelDto(
    val id: String,
    val name: String,
    val iconId: String,
    val colorNumber: Long?,
    val modificationDateTime: String? = null
)
