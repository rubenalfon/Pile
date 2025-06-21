package com.ganadoro.pile.ui.navigation


sealed class NavRoute(val path: String) {

    data object Home : NavRoute("home")

    data object PileDetail : NavRoute("PileDetail") {
        const val PILE_ID_KEY = "pileId"
    }

    data object DocumentDetail : NavRoute("DocumentDetail") {
        const val DOCUMENT_ID_KEY = "documentId"
    }

    data object EditPDF : NavRoute("EditPDF") {
        const val DOCUMENT_ID_KEY = "documentId"
    }

    data object AddDocument : NavRoute("AddDocument") {
        const val DOCUMENT_ID_KEY = "documentId"
    }

    // build navigation path (for screen navigation)
    fun withArgs(vararg args: String): String {
        return buildString {
            append(path)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

    // build and setup route format (in navigation graph)
    fun withArgsFormat(vararg args: String): String {
        return buildString {
            append(path)
            args.forEach { arg ->
                append("/{$arg}")
            }
        }
    }
}