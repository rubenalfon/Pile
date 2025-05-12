package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
    val icon: ImageVector? = null,
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
            val pile3 = Pile(name = "Mis Pilas 3", isSelected = true)

            Pile(modifier = Modifier.width(60.dp), pile = pile1)
            Pile(modifier = Modifier.width(60.dp), pile = pile2)
            Pile(modifier = Modifier.width(60.dp), pile = pile3)
        }
    }
}

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pile: Pile
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            Modifier                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {


            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(
                        5.dp,
                        pile.color ?: MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
            ) {
                if (pile.icon != null) {
                    Icon(
                        imageVector = pile.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Text(
                        pile.name.substring(0, 1).uppercase(),
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
            }

            if (pile.isSelected) {
                Box(
                    Modifier
                        .size(23.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            pile.name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}