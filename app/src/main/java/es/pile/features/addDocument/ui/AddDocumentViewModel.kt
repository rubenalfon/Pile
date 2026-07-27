package es.pile.features.addDocument.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.useCases.CreatePileUseCase
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.features.addDocument.domain.models.DocumentSaveException
import es.pile.features.addDocument.domain.useCases.SaveDocumentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddDocumentViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val saveDocumentUseCase: SaveDocumentUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private var _state = MutableStateFlow(AddDocumentState())
    var state: StateFlow<AddDocumentState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val documentFlow =
                documentModelRepository.getDocumentModelById(documentId).distinctUntilChanged()

            val documentFirstImageFlow = documentFlow
                .map { it?.imageIds?.firstOrNull() }
                .distinctUntilChanged()
                .flatMapLatest { imageId ->
                    if (imageId == null) return@flatMapLatest flowOf(null)

                    documentImageRepository.getDocumentImageById(imageId)
                }

            val allPilesFlow = pileModelRepository.pileModels

            combine(
                documentFlow,
                documentFirstImageFlow,
                allPilesFlow
            ) { document, image, allPiles ->
                if (document == null) return@combine

                val coverImageCacheKey = bitmapCacheRepository.getImageKey(document, 0)

                _state.update {
                    it.copy(
                        documentModel = it.documentModel ?: document,
                        coverDocumentImage = image,
                        coverImageCacheKey = coverImageCacheKey,
                        documentName = it.documentName ?: document.title,
                        allPileModels = allPiles,
                    )
                }
            }.collect()
        }
    }

    fun handleEvent(event: AddDocumentEvent) {
        when (event) {
            AddDocumentEvent.OnImageVisible -> requestBitmapLoad()
            is AddDocumentEvent.OnNameChanged -> setDocumentName(event.name)
            is AddDocumentEvent.OnAddPile -> addRemoveDocumentPiles(event.pileName)
            is AddDocumentEvent.OnCreateNewPile -> createAddPile(
                event.pileName,
                event.iconId,
                event.color
            )

            AddDocumentEvent.OnSaveDocument -> saveDocument()
        }
    }

    private fun requestBitmapLoad() {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch
            requestBitmapLoadUseCase(document, 0)
        }
    }

    private fun setDocumentName(name: String) {
        _state.update {
            it.copy(
                documentName = name,
                noDocumentNameError = name.isBlank()
            )
        }
    }

    private fun addRemoveDocumentPiles(pileId: String) {
        val document = state.value.documentModel ?: return

        val documentPiles = document.documentPileIds

        val updatedDocumentPiles = documentPiles.toMutableList().apply {
            if (contains(pileId)) remove(pileId)
            else add(pileId)
        }

        _state.update { it.copy(documentModel = document.copy(documentPileIds = updatedDocumentPiles)) }
    }

    private fun createAddPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val pileId = createPileUseCase(pileName, iconId, color)
            addRemoveDocumentPiles(pileId)
        }
    }

    private fun saveDocument() {
        val currentState = _state.value
        val documentModel = currentState.documentModel ?: return

        viewModelScope.launch {
            saveDocumentUseCase(documentModel, currentState.documentName ?: "").onSuccess {
                _state.update { it.copy(noDocumentNameError = false) }
                _navigationEvent.send(Unit)
            }.onFailure { exception ->
                when (exception) {
                    is DocumentSaveException.AlreadySaved -> {
                        _navigationEvent.send(Unit)
                    }

                    is DocumentSaveException.EmptyName -> {
                        _state.update { it.copy(noDocumentNameError = true) }
                    }
                }
            }
        }
    }
}