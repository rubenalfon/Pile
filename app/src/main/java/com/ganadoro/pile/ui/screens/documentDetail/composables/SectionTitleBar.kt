package com.ganadoro.pile.ui.screens.documentDetail.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R

@Composable
fun SectionTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    onButtonCLick: (() -> Unit)? = null,
    isSaveMode: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(Modifier
            .height(48.dp)
            .background(Color.Green))
        if (onButtonCLick == null) return@Row

        IconButton(
            onClick = { onButtonCLick.invoke() }
        ) {
            if (isSaveMode)
                Icon(
                    painter = painterResource(R.drawable.check_24px),
                    contentDescription = stringResource(R.string.save_changes_in_document_section_name, title),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            else
                Icon(
                    painter = painterResource(R.drawable.edit_24px),
                    contentDescription = stringResource(R.string.edit_document_section_name, title),
                    tint = MaterialTheme.colorScheme.onSurface
                )
        }
    }
}