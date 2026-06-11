package es.pile.core.ui.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


sealed interface Pane : Parcelable, NavKey {
    @Parcelize
    @Serializable
    data object Home : Pane

    @Parcelize
    @Serializable
    data class PileDetail(
        val pileId: String
    ) : Pane

    @Parcelize
    @Serializable
    data class DocumentDetail(
        val documentId: String
    ) : Pane

    @Parcelize
    @Serializable
    data class EditDocumentPiles(
        val documentId: String
    ) : Pane

    @Parcelize
    @Serializable
    data class EditNewDocument(
        val documentId: String,
    ) : Pane

    @Parcelize
    @Serializable
    data class EditExistingDocument(
        val documentId: String,
    ) : Pane

    @Parcelize
    @Serializable
    data class AddDocument(
        val documentId: String
    ) : Pane

    @Parcelize
    @Serializable
    data class Search(
        val pileId: String? = null
    ) : Pane


    @Parcelize
    @Serializable
    data object SettingsOverview : Pane

    @Parcelize
    @Serializable
    data object SettingsResolution : Pane

    @Parcelize
    @Serializable
    data object SettingsLanguageModel : Pane
}