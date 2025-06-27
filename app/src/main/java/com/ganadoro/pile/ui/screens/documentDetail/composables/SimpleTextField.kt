package com.ganadoro.pile.ui.screens.documentDetail.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun SimpleTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (newValue: String) -> Unit,
    textStyle: TextStyle,
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences
    ),
    hint: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    Box(
        modifier,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(IntrinsicSize.Max),
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            enabled = enabled
        )

        if (hint != null && value.isEmpty())
            Text(
                text = hint,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
    }
}
