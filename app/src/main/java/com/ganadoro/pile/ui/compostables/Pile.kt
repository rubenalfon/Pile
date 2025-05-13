package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class Pile(
    val name: String,
    val icon: ImageVector,
    val color: Color? = null,
    val isSelected: Boolean = false
)

@Preview
@Composable
private fun PileFolder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            val pile1 =
                Pile(name = "Mis Pilas 1", icon = Icons.Default.Add, color = Color.Red)
            val pile2 = Pile(name = "Mis Pilas 2", icon = Icons.Default.Add)
//            val pile3 = Pile(name = "Mis Pilas 3", isSelected = true)

            Pile(modifier = Modifier, pile = pile1)
            Pile(modifier = Modifier, pile = pile2)
//            Pile(modifier = Modifier, pile = pile3)
        }
    }
}

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pile: Pile
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(pile.color ?: MaterialTheme.colorScheme.surfaceVariant)
            .padding(12 .dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = pile.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, // TODO: Hacer que se genere automaticamente
            modifier = Modifier.size(40.dp)
        )
        Text(
            pile.name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Left
        )
    }
}