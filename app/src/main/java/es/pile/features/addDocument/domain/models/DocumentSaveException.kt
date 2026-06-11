package es.pile.features.addDocument.domain.models

sealed class DocumentSaveException(message: String) : Exception(message) {
    object AlreadySaved : DocumentSaveException("The document is already saved")
    object EmptyName : DocumentSaveException("The document name cannot be empty")
}