package com.ganadoro.pile.ui.navigation


sealed class NavRoute(val path: String) {

    data object Home : NavRoute("home")

    data object PileDetail : NavRoute("PileDetail") {
        const val pileId = "pileId"
    }

    data object EditPDF : NavRoute("EditPDF") {
        const val documentId = "documentId"
    }

    data object AddDocument : NavRoute("AddDocument") {
        const val documentId = "documentId"
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