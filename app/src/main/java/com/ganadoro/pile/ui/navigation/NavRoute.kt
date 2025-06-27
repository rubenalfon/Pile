package com.ganadoro.pile.ui.navigation


sealed class NavRoute(val path: String) {

    data object HomeRoute : NavRoute("home")

    data object PileDetailRoute : NavRoute("PileDetail") {
        const val PILE_ID_KEY = "pileId"
    }

    data object DocumentDetailRoute : NavRoute("DocumentDetail") {
        const val DOCUMENT_ID_KEY = "documentId"
    }

    data object EditPDFRoute : NavRoute("EditPDF") {
        const val DOCUMENT_ID_KEY = "documentId"
    }

    data object AddDocumentRoute : NavRoute("AddDocument") {
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