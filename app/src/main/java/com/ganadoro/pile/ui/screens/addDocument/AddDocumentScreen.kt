package com.ganadoro.pile.ui.screens.addDocument

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.smallContainerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.screens.home.compostables.itemPileGrid
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddDocumentScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    navigateToDocumentDetail: (String) -> Unit,
    viewModel: AddDocumentViewModel = getViewModel<AddDocumentViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = documentId) {
        if (uiState.documentModel == null) {
            viewModel.loadDocument(documentId)
        }
        if (uiState.allPileModels == null) {
            viewModel.loadPiles()
        }
        if (viewModel.navigateToDocumentDetail == null) {
            viewModel.navigateToDocumentDetail = navigateToDocumentDetail
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ScreenTopAppBar(popBackStack = popBackStack)
        },
        floatingActionButton = {
            MediumFloatingActionButton(
                onClick = { viewModel.saveDocument() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.check_24px),
                    contentDescription = stringResource(R.string.add_document)
                )
            }
        }
    ) { innerPadding ->
        LoadingWrapper(
            uiState.documentModel == null || uiState.allPileModels == null // TODO Bitmap ?
        ) {
            var availableWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            val colorScheme = MaterialTheme.colorScheme

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .onGloballyPositioned { coordinates ->
                        val widthPx = coordinates.size.width
                        availableWidth = with(density) { widthPx.toDp() }.value.dp
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    ) {
                        LoadingWrapper(uiState.firstPageBitmap == null) {
                            Image(
                                bitmap = uiState.firstPageBitmap!!.asImageBitmap(),
                                contentDescription = stringResource(R.string.document_first_image),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    OutlinedTextField(
                        value = uiState.documentName,
                        onValueChange = { viewModel.setDocumentName(it) },
                        label = { Text(stringResource(R.string.document_name)) },
                        trailingIcon = {
                            if (uiState.documentName.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.delete_text),
                                    modifier = Modifier.clickable { viewModel.setDocumentName("") })
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        isError = uiState.noDocumentNameError,
                        supportingText = {
                            if (uiState.noDocumentNameError) {
                                Text(stringResource(R.string.document_no_name_error))
                            }
                        }
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    Text(
                        text = stringResource(R.string.add_to_piles),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(colorScheme.surfaceContainer)
                            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                    )
                }

                itemPileGrid(
                    availableWidth = availableWidth,
                    piles = uiState.allPileModels!!,
                    onPileClick = { pileId ->
                        viewModel.updatePileSelectState(pileId)
                    },
                    coloredPileIds = uiState.selectedPileModelIds,
                    backgroundColor = colorScheme.surfaceContainer
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun ScreenTopAppBar(
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(stringResource(R.string.add_document))
        },
        navigationIcon = {
            FilledIconButton(
                modifier = Modifier
                    .padding(start = 14.dp, end = 4.dp)
                    .size(smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                onClick = popBackStack
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.return_)
                )
            }
        },
        colors = topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
