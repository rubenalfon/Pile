package es.pile.core.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import es.pile.features.addDocument.ui.AddDocumentScreen
import es.pile.features.documentDetail.ui.DocumentDetailScreen
import es.pile.features.editDocument.ui.EditDocumentScreen
import es.pile.features.home.ui.HomeScreen
import es.pile.features.pileDetail.ui.PileDetailScreen
import es.pile.features.search.ui.SearchScreen
import es.pile.features.settings.ui.overview.SettingsOverviewScreen
import es.pile.features.settings.ui.resolution.SettingsResolutionScreen

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
                    navigateToEditDocument = { id ->
                        backStack.add(Pane.EditNewDocument(documentId = id))
                    },
                    navigateToAddDocument = { id ->
                        backStack.add(Pane.AddDocument(documentId = id))
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
                    navigateToSearchScreen = {id ->
                        backStack.add(Pane.Search(pileId = id))
                    },
                    navigateToEditDocument = { id ->
                        backStack.add(Pane.EditNewDocument(documentId = id))
                    },
                    navigateToAddDocument = { id ->
                        backStack.add(Pane.AddDocument(documentId = id))
                    },
                    popBackStack = {
                        backStack.removeLastOrNull()
                    },
                    popToHome = {
                        backStack.removeIf {
                            it !is Pane.Home
                        }
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

            entry<Pane.Search> { backStackKey ->
                SearchScreen(
                    pileId = backStackKey.pileId,
                    onBack = { backStack.removeLastOrNull() },
                    navigateToDocumentDetail = { id ->
                        backStack.add(Pane.DocumentDetail(documentId = id))
                    }
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