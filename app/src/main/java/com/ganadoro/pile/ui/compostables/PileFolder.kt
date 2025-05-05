package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun PileFolderPreview() {
    Surface (Modifier.background(MaterialTheme.colorScheme.background)) {
        Row {
            repeat(4) {
                PileFolder(
                    name = "Mis Pilas",
                    icon = Icons.Default.Add,
                    isColorful = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PileFolder(
    name: String,
    icon: ImageVector? = null,
    isColorful: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(5.dp, Color.Red, CircleShape)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxSize().padding()
                    )
                } else {
                    Text(name)
                }
            }

        }
        Text(name)
    }
}