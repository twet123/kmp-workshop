package org.example.air_workshop.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.air_workshop.model.Tag
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class RoomTagRepositoryTest {

    private val database = createInMemoryDatabase()
    private val repository = RoomTagRepository(database.tagDao())

    private fun createTag(
        id: Long = 0L,
        name: String = "Groceries",
        colorHex: String = "#FF5722"
    ) = Tag(id, name, colorHex)

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertTag_returnsPositiveId() = runTest {
        val tag = createTag()
        val id = repository.insertTag(tag)
        assertTrue(id > 0)
    }

    @Test
    fun insertAndGetTagById_preservesFields() = runTest {
        val tag = createTag(name = "Transport", colorHex = "#2196F3")
        val id = repository.insertTag(tag)
        val fetched = repository.getTagById(id)
        assertNotNull(fetched)
        assertEquals("Transport", fetched.name)
        assertEquals("#2196F3", fetched.colorHex)
    }

    @Test
    fun getTagById_nonExistent_returnsNull() = runTest {
        assertNull(repository.getTagById(999L))
    }

    @Test
    fun updateTag_modifiesFields() = runTest {
        val tag = createTag(name = "Old")
        val id = repository.insertTag(tag)
        repository.updateTag(createTag(id = id, name = "New", colorHex = "#000000"))
        val fetched = repository.getTagById(id)
        assertNotNull(fetched)
        assertEquals("New", fetched.name)
        assertEquals("#000000", fetched.colorHex)
    }

    @Test
    fun deleteTag_removesRecord() = runTest {
        val tag = createTag()
        val id = repository.insertTag(tag)
        repository.deleteTag(tag.copy(id = id))
        assertNull(repository.getTagById(id))
    }

    @Test
    fun deleteTagById_removesRecord() = runTest {
        val tag = createTag()
        val id = repository.insertTag(tag)
        repository.deleteTagById(id)
        assertNull(repository.getTagById(id))
    }

    @Test
    fun observeAllTags_returnsAllInserted() = runTest {
        repository.insertTag(createTag(name = "A"))
        repository.insertTag(createTag(name = "B"))
        val all = repository.observeAllTags().first()
        assertEquals(2, all.size)
    }

    @Test
    fun observeAllTags_empty_returnsEmptyList() = runTest {
        val all = repository.observeAllTags().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun getAllTags_returnsAllInserted() = runTest {
        repository.insertTag(createTag(name = "Food"))
        repository.insertTag(createTag(name = "Rent"))
        val all = repository.getAllTags()
        assertEquals(2, all.size)
    }

    @Test
    fun getAllTags_empty_returnsEmptyList() = runTest {
        assertTrue(repository.getAllTags().isEmpty())
    }

    @Test
    fun observeAllTags_orderedByNameAsc() = runTest {
        repository.insertTag(createTag(name = "Zebra"))
        repository.insertTag(createTag(name = "Apple"))
        val all = repository.observeAllTags().first()
        assertEquals("Apple", all[0].name)
        assertEquals("Zebra", all[1].name)
    }
}
