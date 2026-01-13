package com.ganadoro.pile.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ganadoro.pile.ui.screens.addDocument.AddDocumentScreen
import com.ganadoro.pile.ui.screens.documentDetail.DocumentDetailScreen
import com.ganadoro.pile.ui.screens.editDocument.EditDocumentScreen
import com.ganadoro.pile.ui.screens.home.HomeScreen
import com.ganadoro.pile.ui.screens.pileDetail.PileDetailScreen
import com.ganadoro.pile.ui.screens.search.SearchBarScreen
import comganadoro.pile.ui.screens.editDocumentPiles.EditDocumentPilesScreen
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PileNavigation(modifier: Modifier = Modifier, backStack: NavBackStack<NavKey>) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            // Add the default decorators for managing scenes and saving state
            rememberSaveableStateHolderNavEntryDecorator(),
            // Then add the view model store decorator
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it / 5 }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { it / 5 }) + fadeOut()
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { it / 5 }) + fadeOut()
        },
        entryProvider = entryProvider {
            entry<Pane.Home> {
                HomeScreen(
                    navigateToPileDetail = { id ->
                        backStack.add(Pane.PileDetail(pileId = id))
                    },
                    navigateToDocumentDetail = { id ->
                        backStack.add(Pane.DocumentDetail(documentId = id))
                    },
                    navigateToEditPDF = { id ->
                        backStack.add(Pane.EditNewPDF(documentId = id))
                    },
                    navigateToAddDocument = {
                        backStack.add(Pane.AddDocument(documentId = it))
                    }
                )
            }

            entry<Pane.PileDetail> { backStackKey ->
                PileDetailScreen(
                    pileId = backStackKey.pileId,
                    navigateToDocumentDetail = { id ->
                        backStack.add(Pane.DocumentDetail(documentId = id))
                    },
                    navigateToSearchScreen = {
                        backStack.add(Pane.Search)
                    },

                    popBackStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Pane.DocumentDetail> { backStackKey ->
                DocumentDetailScreen(
                    documentId = backStackKey.documentId,
                    navigateToPileDetail = { id ->
                        backStack.add(Pane.PileDetail(pileId = id))
                    },
                    navigateToEditDocument = { id ->
                        backStack.add(Pane.EditExistingPDF(documentId = id))
                    },
                    popBackStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Pane.EditDocumentPiles> { backStackKey ->
                EditDocumentPilesScreen(
                    documentId = backStackKey.documentId,
                    popBackStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Pane.EditExistingPDF> { backStackKey ->
                EditDocumentScreen(
                    documentId = backStackKey.documentId,
                    popBackStack = {
                        backStack.removeLastOrNull()
                    },
                    onNext = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Pane.EditNewPDF> { backStackKey ->
                EditDocumentScreen(
                    documentId = backStackKey.documentId,
                    popBackStack = {
                        backStack.removeLastOrNull()
                    },
                    onNext = {
                        backStack.add(Pane.AddDocument(documentId = backStackKey.documentId))
                    }
                )
            }

            entry<Pane.AddDocument> { backStackKey ->
                AddDocumentScreen(
                    documentId = backStackKey.documentId,
                    popBackStack = {
                        backStack.removeLastOrNull()
                    },
                    navigateToDocumentDetail = {
                        backStack.removeLastOrNull()
                        backStack.removeLastOrNull()
                        backStack.add(
                            Pane.DocumentDetail(
                                documentId = it
                            )
                        )
                    }
                )
            }

            entry<Pane.Search> {
                val showKeyboard = remember { mutableStateOf(true) }
                val focusRequester = remember { FocusRequester() }
                val keyboard = LocalSoftwareKeyboardController.current

                LaunchedEffect(focusRequester) {
                    if (showKeyboard.value) {
                        focusRequester.requestFocus()
                        delay(100)
                        keyboard?.show()
                    }
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.displayCutout,
                    topBar = {
                        SearchBarScreen(
                            expanded = true,
                            onExpandedChange = {
                                if (!it) backStack.removeLastOrNull()
                            },
                            onSettingsClick = {}, // Do not
                            navigateToDocumentDetail = { id ->
                                backStack.add(
                                    Pane.DocumentDetail(
                                        documentId = id
                                    )
                                )
                            },
                            focusRequester = focusRequester
                        )
                    },
                    content = {}
                )
            }
        }
    )
}