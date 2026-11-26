package es.pile.features.documentDetail.ui.models

import androidx.compose.foundation.text.input.TextFieldState
import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.models.StringDetail

fun DocumentDetail.toUiState(): DetailItemUiState = when (this) {
    is StringDetail -> DetailItemUiState.Text(
        id = id,
        nameState = TextFieldState(name),
        valueState = TextFieldState(value)
    )
// is DateDetail -> DetailItemUiState.Date(
    //     id = id,
    //     nameState = TextFieldState(name),
    //     value = value
    // )
}

fun DetailItemUiState.toDomain(): DocumentDetail = when (this) {
    is DetailItemUiState.Text -> StringDetail(
        id = id,
        name = nameState.text.toString(),
        value = valueState.text.toString()
    )
    // is DetailItemUiState.Date -> DateDetail(
    //     id = id,
    //     name = nameState.text.toString(),
    //     value = value
    // )
}
