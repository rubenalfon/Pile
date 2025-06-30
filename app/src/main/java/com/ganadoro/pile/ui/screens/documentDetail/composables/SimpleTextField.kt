package com.ganadoro.pile.ui.screens.documentDetail.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign

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
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        enabled = enabled,
        decorationBox = { innerTextField ->
            val contentAlignment: Alignment =
            when (textStyle.textAlign) {
                TextAlign.Center -> Alignment.Center
                TextAlign.Right -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }
            Box(
                modifier = Modifier.width(IntrinsicSize.Min),
                contentAlignment = contentAlignment
            ) {
                innerTextField()

                if (value.isEmpty() && hint != null) {
                    Text(
                        text = hint,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                    )
                }
            }
        }
    )
}
