package es.pile.features.documentDetail.domain.useCases

import es.pile.core.domain.models.StringDetail
import es.pile.features.documentDetail.ui.DetailsActionEvent
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateDocumentDetailsUseCaseTest {

    private val useCase = UpdateDocumentDetailsUseCase()

    @Test
    fun `OnNew should add a new empty detail`() {
        val current = emptyList<es.pile.core.domain.models.DocumentDetail>()
        val result = useCase(current, emptyList(), DetailsActionEvent.OnNew)
        
        assertEquals(1, result.updatedDetails.size)
        assertTrue(result.updatedDetails[0] is StringDetail)
        assertEquals("", (result.updatedDetails[0] as StringDetail).name)
    }

    @Test
    fun `OnRemove should remove item and add it to stack`() {
        val item = StringDetail("1", "Name", "Value")
        val current = listOf(item)
        val result = useCase(current, emptyList(), DetailsActionEvent.OnRemove(0))
        
        assertEquals(0, result.updatedDetails.size)
        assertEquals(1, result.updatedDeletedStack.size)
        assertEquals(item, result.updatedDeletedStack[0])
    }

    @Test
    fun `OnRestore should move item from stack back to details`() {
        val item = StringDetail("1", "Name", "Value")
        val result = useCase(emptyList(), listOf(item), DetailsActionEvent.OnRestore)
        
        assertEquals(1, result.updatedDetails.size)
        assertEquals(0, result.updatedDeletedStack.size)
        assertEquals("Name", (result.updatedDetails[0] as StringDetail).name)
    }
    
    @Test
    fun `OnIndexMove should reorder items`() {
        val item1 = StringDetail("1", "N1", "V1")
        val item2 = StringDetail("2", "N2", "V2")
        val current = listOf(item1, item2)
        
        val result = useCase(current, emptyList(), DetailsActionEvent.OnIndexMove(0, 1))
        
        assertEquals(listOf(item2, item1), result.updatedDetails)
    }
}
