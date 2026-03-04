package com.ganadoro.pile.features.addDocument.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.PileModelRepository
import com.ganadoro.pile.core.domain.useCases.CreatePileUseCase
import com.ganadoro.pile.core.domain.useCases.ManageDocumentPileUseCase
import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.ganadoro.pile.features.addDocument.domain.models.DocumentSaveException
import com.ganadoro.pile.features.addDocument.domain.useCases.SaveDocumentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddDocumentUiState(
    val documentModel: DocumentModel? = null,
    val frontPageDocumentImage: DocumentImage? = null,
    val documentName: String = "",
    val allPileModels: List<PileModel>? = null,
    val noDocumentNameError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AddDocumentViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val manageDocumentPileUseCase: ManageDocumentPileUseCase,
    private val saveDocumentUseCase: SaveDocumentUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(AddDocumentUiState())
    var uiState: StateFlow<AddDocumentUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val documentFlow =
                documentModelRepository.getDocumentModelById(documentId).distinctUntilChanged()

            val documentFirstImageFlow = documentFlow
                .mapNotNull { it?.imageIds?.firstOrNull() }
                .distinctUntilChanged()
                .flatMapLatest { imageId ->
                    documentImageRepository.getDocumentImageById(imageId)
                }

            combine(documentFlow, documentFirstImageFlow) { document, image ->
                if (document == null) return@combine

                _uiState.update {
                    it.copy(
                        documentModel = document,
                        documentName = document.title,
                        frontPageDocumentImage = image,
                        allPileModels = pileModelRepository.getAllPileModels(),
                    )
                }
            }.collect()
        }
    }

    fun requestBitmapLoad() {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            requestBitmapLoadUseCase(document, 0)
        }
    }

    fun requestImageKey(): String {
        val document = uiState.value.documentModel ?: return ""
        return bitmapCacheRepository.getImageKey(document, 0)
    }

    fun setDocumentName(name: String) {
        _uiState.update {
            it.copy(
                documentName = name,
                noDocumentNameError = name.isBlank()
            )
        }
    }

    fun addRemoveDocumentPiles(pileId: String) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            manageDocumentPileUseCase(document, pileId)
        }
    }

    fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val pileId = createPileUseCase(pileName, iconId, color)
            addRemoveDocumentPiles(pileId)
        }
    }

    fun saveDocument() {
        val currentState = _uiState.value
        val documentModel = currentState.documentModel ?: return

        viewModelScope.launch {
            saveDocumentUseCase(documentModel, currentState.documentName).onSuccess {
                _uiState.update { it.copy(noDocumentNameError = false) }
                _navigationEvent.send(Unit)
            }.onFailure { exception ->
                when (exception) {
                    is DocumentSaveException.AlreadySaved -> {
                        _navigationEvent.send(Unit)
                    }

                    is DocumentSaveException.EmptyName -> {
                        _uiState.update { it.copy(noDocumentNameError = true) }
                    }
                }
            }
        }
    }
}