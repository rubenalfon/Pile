package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Preview
@Composable
private fun DocumentsDividerPreview() {
    Surface(Modifier.fillMaxSize()) {
        DocumentsDivider(
            date = LocalDate.now()
        )
    }
}


@Composable
fun DocumentsDivider(
    modifier: Modifier = Modifier,
    date: LocalDate,
    showDivider: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val locale = Locale.getDefault()
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)

        Text(text = date.format(formatter), color = MaterialTheme.colorScheme.outline)

        if (showDivider)
            HorizontalDivider()
    }
}