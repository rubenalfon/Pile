package es.pile.features.documentDetail.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pile.R

@Composable
fun SectionTitleBar(
    modifier: Modifier = Modifier,
    title: String,
    onButtonCLick: (() -> Unit)? = null,
    isSaveMode: Boolean = false
) {
    Row(
        modifier = modifier.height(48.dp).padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (onButtonCLick == null) return@Row

        val containerColor by animateColorAsState(
            targetValue = if (isSaveMode) MaterialTheme.colorScheme.primary else Color.Transparent,
            label = "containerColor"
        )
        val contentColor by animateColorAsState(
            targetValue = if (isSaveMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            label = "contentColor"
        )

        IconButton(
            onClick = { onButtonCLick.invoke() },
            colors = IconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = Color.Red,
                disabledContentColor = Color.Red
            )
        ) {
            if (isSaveMode)
                Icon(
                    painter = painterResource(R.drawable.check_24px),
                    contentDescription = stringResource(
                        R.string.save_changes_in_document_section_name,
                        title
                    ),
                )
            else
                Icon(
                    painter = painterResource(R.drawable.edit_24px),
                    contentDescription = stringResource(R.string.edit_document_section_name, title),
                )
        }
    }
}