package es.pile.features.search.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.models.StringDetail
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchDocumentsUseCaseTest {

    private val searchDocumentsUseCase = SearchDocumentsUseCase()

    private val date1 = LocalDate.of(2023, 10, 1)

    private val doc1 = mockk<DocumentModel> {
        every { title } returns "Document Alpha"
        every { documentNote } returns "Notes about alpha"
        every { documentPileIds } returns listOf("pile1")
        every { creationDateTime } returns LocalDateTime.of(2023, 10, 1, 10, 0)
        every { modificationDateTime } returns LocalDateTime.of(2023, 10, 1, 12, 0)
        every { documentDetails } returns listOf(StringDetail("id1", "Name", "ValueX"))
    }

    private val doc2 = mockk<DocumentModel> {
        every { title } returns "Document Beta"
        every { documentNote } returns "Notes about beta"
        every { documentPileIds } returns listOf("pile2")
        every { creationDateTime } returns LocalDateTime.of(2023, 10, 2, 10, 0)
        every { modificationDateTime } returns LocalDateTime.of(2023, 10, 2, 12, 0)
        every { documentDetails } returns emptyList()
    }

    private val documentList = listOf(doc1, doc2)

    @Test
    fun `return empty when no filters applied`() {
        val result = searchDocumentsUseCase.execute(documentList, "", emptyList(), null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter by query only`() {
        val result = searchDocumentsUseCase.execute(documentList, "Alpha", emptyList(), null)
        assertEquals(1, result.size)
        assertEquals("Document Alpha", result[0].title)
    }

    @Test
    fun `filter by pile only`() {
        val result = searchDocumentsUseCase.execute(documentList, "", listOf("pile2"), null)
        assertEquals(1, result.size)
        assertEquals("Document Beta", result[0].title)
    }

    @Test
    fun `filter by date only`() {
        val result = searchDocumentsUseCase.execute(documentList, "", emptyList(), date1)
        assertEquals(1, result.size)
        assertEquals("Document Alpha", result[0].title)
    }

    @Test
    fun `filter by query and pile`() {
        val result = searchDocumentsUseCase.execute(documentList, "Document", listOf("pile1"), null)
        assertEquals(1, result.size)
        assertEquals("Document Alpha", result[0].title)
    }

    @Test
    fun `filter by query in details`() {
        val result = searchDocumentsUseCase.execute(documentList, "ValueX", emptyList(), null)
        assertEquals(1, result.size)
        assertEquals("Document Alpha", result[0].title)
    }
}
