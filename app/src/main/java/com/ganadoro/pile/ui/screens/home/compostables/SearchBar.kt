package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val animatedColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "SearchBarColor"
    )

    SearchBar(
        modifier = modifier
            .fillMaxWidth(),
        inputField = {
            SearchBarDefaults.InputField(
                query = text,
                onQueryChange = { text = it },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Hinted search text") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(animatedColor)
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
//        Column(Modifier.verticalScroll(rememberScrollState())) {
//            repeat(4) { idx ->
//                val resultText = "Suggestion $idx"
//                ListItem(
//                    headlineContent =
//                        {
//                            Text(resultText)
//                        },
//                    supportingContent = { Text("Additional info") },
//                    leadingContent = {
//                        Icon(
//                            Icons.Filled.Star,
//                            contentDescription = null
//                        )
//                    },
//                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
//                    modifier = Modifier
//                        .clickable {
//                            text = resultText
//                            expanded = false
//                        }
//                        .fillMaxWidth()
////                        .padding(horizontal = 16.dp, vertical = 4.dp)
//                )
//            }
//        }
    }
}