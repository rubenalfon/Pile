package es.pile.features.documentDetail.ui.models

import androidx.compose.foundation.text.input.TextFieldState

/**
 * Represents the UI state of a single document detail item in the list.
 */
sealed interface DetailItemUiState {
    /**
     * Unique identifier of the detail item.
     */
    val id: String

    /**
     * The state of the text field for the detail's name (or title).
     */
    val nameState: TextFieldState

    /**
     * Represents a detail item that consists of a simple text value.
     *
     * @property id Unique identifier of the detail item.
     * @property nameState The state of the text field for the detail's name.
     * @property valueState The state of the text field for the detail's value.
     */
    data class Text(
        override val id: String,
        override val nameState: TextFieldState,
        val valueState: TextFieldState
    ) : DetailItemUiState

    // data class Date(
    //     override val id: String,
    //     override val nameState: TextFieldState,
    //     val value: LocalDate
    // ) : DetailItemUiState
}
