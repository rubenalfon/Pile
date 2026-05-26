package com.pile.core.ui.navigation

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
import com.pile.features.addDocument.ui.AddDocumentScreen
import com.pile.features.documentDetail.ui.DocumentDetailScreen
import com.pile.features.editDocument.ui.EditDocumentScreen
import com.pile.features.home.ui.HomeScreen
import com.pile.features.pileDetail.ui.PileDetailScreen
import com.pile.features.search.ui.SearchBarScreen
import com.pile.features.settings.ui.overview.SettingsOverviewScreen
import com.pile.features.settings.ui.resolution.SettingsResolutionScreen
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
                        backStack.add(Pane.EditNewDocument(documentId = id))
                    },
                    navigateToAddDocument = {
                        backStack.add(Pane.AddDocument(documentId = it))
                    },
                    navigateToSettings = {
                        backStack.add(Pane.SettingsOverview)
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
                        backStack.add(Pane.EditExistingDocument(documentId = id))
                    },
                    popBackStack = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Pane.EditExistingDocument> { backStackKey ->
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

            entry<Pane.EditNewDocument> { backStackKey ->
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
                    navigateToDocumentDetail = { documentId ->
                        backStack.removeIf {
                            it !is Pane.Home
                        }
                        backStack.add(
                            Pane.DocumentDetail(
                                documentId = documentId
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

            entry<Pane.SettingsOverview> {
                SettingsOverviewScreen(
                    popBackStack = backStack::removeLastOrNull,
                    navigateToSettingsResolution = {
                        backStack.add(Pane.SettingsResolution)
                    }
                )
            }

            entry<Pane.SettingsResolution> {
                SettingsResolutionScreen(
                    popBackStack = backStack::removeLastOrNull
                )
            }
        }
    )
}