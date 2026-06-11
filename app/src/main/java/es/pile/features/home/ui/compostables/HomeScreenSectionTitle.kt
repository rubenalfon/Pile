package es.pile.features.home.ui.compostables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun HomeScreenSectionTitlePreview() {
    Surface(Modifier.fillMaxSize()) {
        HomeScreenSectionTitle(
            title = "Mis Pilas",
            trailingButtonText = "Ver más",
            trailingButtonOnClick = {}
        )
    }
}

@Composable
fun HomeScreenSectionTitle(
    modifier: Modifier = Modifier,
    title: String,
    trailingButtonText: String? = null,
    trailingButtonOnClick: (() -> Unit)? = null
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )

        if (trailingButtonText != null && trailingButtonOnClick != null) {
            TextButton(
                onClick = {
                    trailingButtonOnClick()
                }
            ) {
                Text(
                    text = trailingButtonText,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

    }
}