package es.pile.features.documentDetail.domain.useCases

import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.models.StringDetail
import es.pile.features.documentDetail.domain.models.DetailsCollectionResult
import es.pile.features.documentDetail.ui.DetailsActionEvent
import java.util.UUID

/**
 * Use case responsible for managing the logic of document details.
 * It handles CRUD operations, reordering, and the Undo/Restore stack logic.
 * This class is stateless; it processes inputs and returns the new state.
 */
class UpdateDocumentDetailsUseCase {
    /**
     * Processes a UI event and calculates the new state for details and the undo stack.
     *
     * @param currentDetails The current list of details shown to the user.
     * @param deletedStack The current stack of deleted items (for undo functionality).
     * @param event The event triggered by the user.
     * @return A [DetailsCollectionResult] containing the new list and new stack.
     */
    operator fun invoke(
        currentDetails: List<DocumentDetail>,
        deletedStack: List<DocumentDetail>,
        event: DetailsActionEvent
    ): DetailsCollectionResult {
        return when (event) {
            is DetailsActionEvent.OnIndexMove -> {
                DetailsCollectionResult(
                    updatedDetails = moveItem(
                        currentDetails = currentDetails,
                        fromIndex = event.fromIndex,
                        toIndex = event.toIndex
                    ),
                    updatedDeletedStack = deletedStack
                )
            }

            is DetailsActionEvent.OnIdMove -> {
                DetailsCollectionResult(
                    updatedDetails = moveItemById(
                        currentDetails = currentDetails,
                        fromId = event.fromId,
                        toId = event.toId
                    ),
                    updatedDeletedStack = deletedStack
                )
            }

            is DetailsActionEvent.OnUpdateText -> {
                DetailsCollectionResult(
                    updatedDetails = updateText(
                        list = currentDetails,
                        id = event.id,
                        name = event.newName,
                        value = event.newValue
                    ),
                    updatedDeletedStack = deletedStack
                )
            }

            is DetailsActionEvent.OnNew -> {
                DetailsCollectionResult(
                    updatedDetails = addItem(currentDetails),
                    updatedDeletedStack = deletedStack
                )
            }

            is DetailsActionEvent.OnRemove -> {
                val item = currentDetails.getOrNull(event.index)
                    ?: return DetailsCollectionResult(
                        updatedDetails = currentDetails,
                        updatedDeletedStack = deletedStack
                    )

                val updatedDetails = currentDetails.filterIndexed { i, _ -> i != event.index }
                val updatedDeletedStack = deletedStack + item

                DetailsCollectionResult(
                    updatedDetails = updatedDetails,
                    updatedDeletedStack = updatedDeletedStack
                )
            }

            is DetailsActionEvent.OnRestore -> {
                if (deletedStack.isEmpty()) {
                    return DetailsCollectionResult(
                        updatedDetails = currentDetails,
                        updatedDeletedStack = deletedStack
                    )
                }

                val lastDeleted = deletedStack.first()
                val restoredDocumentDetail = when (lastDeleted) {
                    is StringDetail -> lastDeleted.copy(id = UUID.randomUUID().toString())
                    else -> lastDeleted
                }

                val updatedDetails = currentDetails + restoredDocumentDetail
                val updatedDeletedStack = deletedStack - lastDeleted


                DetailsCollectionResult(
                    updatedDetails = updatedDetails,
                    updatedDeletedStack = updatedDeletedStack
                )
            }

            is DetailsActionEvent.OnPurge -> {
                if (deletedStack.isEmpty()) {
                    return DetailsCollectionResult(
                        updatedDetails = currentDetails,
                        updatedDeletedStack = deletedStack
                    )
                }

                val lastDeleted = deletedStack.first()
                val updatedDetails = currentDetails - lastDeleted
                val updatedDeletedStack = deletedStack - lastDeleted

                DetailsCollectionResult(
                    updatedDetails = updatedDetails,
                    updatedDeletedStack = updatedDeletedStack
                )
            }
        }
    }

    /**
     * Moves an item in the details list.
     *
     * @param currentDetails The current list of details.
     * @param fromIndex The index of the item to be moved.
     * @param toIndex The index where the item should be moved.
     * @return A new list with the item moved.
     */
    private fun moveItem(
        currentDetails: List<DocumentDetail>,
        fromIndex: Int,
        toIndex: Int
    ): List<DocumentDetail> =
        currentDetails.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }.toList()

    /**
     * Moves an item in the details list by its ID.
     *
     * @param currentDetails The current list of details.
     * @param fromId The ID of the item to be moved.
     * @param toId The ID where the item should be moved.
     * @return A new list with the item moved.
     */
    private fun moveItemById(
        currentDetails: List<DocumentDetail>,
        fromId: String,
        toId: String
    ): List<DocumentDetail> =
        currentDetails.toMutableList().apply {
            val fromIndex = indexOfFirst { it.id == fromId }
            val toIndex = indexOfFirst { it.id == toId }
            add(toIndex, removeAt(fromIndex))
        }.toList()

    /**
     * Updates the text of a detail item.
     *
     * @param list The list of details.
     * @param id The ID of the item to be updated.
     * @param name The new name of the item.
     * @param value The new value of the item.
     * @return A new list with the updated item.
     */
    private fun updateText(
        list: List<DocumentDetail>,
        id: String,
        name: String,
        value: String
    ): List<DocumentDetail> = list.map { item ->
        if (item.id == id && item is StringDetail) {
            item.copy(name = name, value = value)
        } else item
    }

    /**
     * Adds a new detail item to the list.
     *
     * @param list The current list of details.
     * @return A new list with the new item added.
     */
    private fun addItem(list: List<DocumentDetail>): List<DocumentDetail> = list + StringDetail(
        id = UUID.randomUUID().toString(),
        name = "",
        value = ""
    )
}