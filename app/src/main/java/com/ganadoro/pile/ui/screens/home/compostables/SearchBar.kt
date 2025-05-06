package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var text by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
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
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            repeat(4) { idx ->
                val resultText = "Suggestion $idx"
                ListItem(
                    headlineContent =
                        {
                            Text(resultText)
                        },
                    supportingContent = { Text("Additional info") },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .clickable {
                            text = resultText
                            expanded = false
                        }
                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
//        LazyColumn(
//            contentPadding = PaddingValues(
//                start = 16.dp,
//                top = 72.dp,
//                end = 16.dp,
//                bottom = 16.dp
//            ),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.semantics {
//                traversalIndex = 1f
//            },
//        ) {
//            val list = List(100) { "Text $it" }
//            items(count = list.size) {
//                Text(
//                    text = list[it],
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                )
//            }
//        }
//    }
}