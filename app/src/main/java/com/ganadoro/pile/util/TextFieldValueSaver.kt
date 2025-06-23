package com.ganadoro.pile.util

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

val TextFieldValueSaver = Saver<TextFieldValue, String>(
    save = { it.text },
    restore = { TextFieldValue(it, TextRange(it.length)) }
)
