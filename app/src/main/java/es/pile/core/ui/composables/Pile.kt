package es.pile.core.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import es.pile.PileModel
import es.pile.core.ui.theme.AppIcons
import es.pile.core.ui.theme.ExtendedTheme
import es.pile.core.ui.theme.PileTheme

@Preview(showBackground = true)
@Composable
fun PilePreview() {
    PileTheme {
        val samplePile = PileModel(
            id = "1",
            name = "Documentos de Trabajo",
            iconId = "Bank",
            colorNumber = 1L
        )

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Estado Normal (Sin seleccionar):",
                style = MaterialTheme.typography.labelSmall
            )
            Pile(
                pileModel = samplePile,
                isColored = false
            )

            Text(
                "Estado Coloreado (Seleccionado):",
                style = MaterialTheme.typography.labelSmall
            )
            Pile(
                pileModel = samplePile,
                isColored = true
            )
        }
    }
}

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    isColored: Boolean,
    enabled: Boolean = true,
    customShape: Shape? = null,
    onClick: (String) -> Unit = {}
) {
    val colorIndex = remember(pileModel.colorNumber) {
        pileModel.colorNumber?.toInt()
    }
    val colored = isColored && colorIndex != null

    val backgroundColor by animateColorAsState(
        targetValue = if (colored) {
            ExtendedTheme.colors.customColorList.getOrNull(colorIndex)?.colorContainer
                ?: MaterialTheme.colorScheme.surface
        } else MaterialTheme.colorScheme.surface,
        label = "backgroundColor"
    )
    val foregroundColor by animateColorAsState(
        targetValue = if (colored) {
            ExtendedTheme.colors.customColorList.getOrNull(colorIndex)?.onColorContainer
                ?: MaterialTheme.colorScheme.onSurface
        } else MaterialTheme.colorScheme.onSurface,
        label = "foregroundColor"
    )

    val shapeProgress by animateFloatAsState(
        targetValue = if (isColored) 0f else 1f,
        label = "shapeProgress"
    )

    val density = LocalDensity.current

    val smartShape = customShape ?: remember(density) {
        RoundedCornerShape(
            corner = object : CornerSize {
                override fun toPx(shapeSize: Size, density: Density): Float {
                    val minRadius = with(density) { 8.dp.toPx() }
                    val maxRadius = shapeSize.height / 2f

                    return minRadius + (maxRadius - minRadius) * shapeProgress
                }
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(smartShape)
            .clickable(enabled) { onClick(pileModel.id) }
            .background(backgroundColor)
            .padding(12.dp)
            .animateContentSize()
    ) {
        Icon(
            painter = painterResource(AppIcons.getById(pileModel.iconId)),
            contentDescription = null,
            tint = foregroundColor,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.8f)
        )

        Text(
            text = pileModel.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Start,
            color = foregroundColor,
            modifier = Modifier.padding(horizontal = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}