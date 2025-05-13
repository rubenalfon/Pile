package com.ganadoro.pile.ui.navigation


sealed class NavRoute(val path: String) {

    data object Home : NavRoute("home")
    data object EditPiles : NavRoute("editPiles")

//    data object Pile : NavRoute("Pile") {
//        const val id = "id"
//    }
//
//    data object Document : NavRoute("Document") {
//        const val id = "id"
//    }

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