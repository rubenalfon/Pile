package es.pile.features.editDocument.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.ui.theme.PileTheme

@Preview
@Composable
private fun AddItemCarouselPrev() {
    PileTheme {
        Surface(Modifier.fillMaxSize()) {
            AddItemCarousel { }
        }
    }
}

@Composable
fun AddItemCarousel(
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .clickable { onItemClick.invoke() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            contentDescription = stringResource(R.string.add_image),
            modifier = Modifier.size(40.dp)
        )
    }
}