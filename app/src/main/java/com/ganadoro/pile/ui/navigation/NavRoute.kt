package com.ganadoro.pile.ui.navigation

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
    data class EditNewPDF(
        val documentId: String,
    ) : Pane

    @Parcelize
    @Serializable
    data class EditExistingPDF(
        val documentId: String,
    ) : Pane

    @Parcelize
    @Serializable
    data class AddDocument(
        val documentId: String
    ) : Pane

    @Parcelize
    @Serializable
    data object Search : Pane
}