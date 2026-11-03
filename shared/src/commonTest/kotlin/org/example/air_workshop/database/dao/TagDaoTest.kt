package org.example.air_workshop.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.air_workshop.database.entities.TagEntity
import org.example.air_workshop.test.RoomTest
import org.example.air_workshop.test.createInMemoryDatabase
import kotlin.test.*

class TagDaoTest : RoomTest() {

    private val database = createInMemoryDatabase()
    private val dao = database.tagDao()

    private fun createTag(
        id: Long = 0L,
        name: String = "Groceries",
        colorHex: String = "#FF5722"
    ) = TagEntity(id, name, colorHex)

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun insertTag_returnsPositiveId() = runTest {
        val tag = createTag()
        val id = dao.insertTag(tag)
        assertTrue(id > 0, "Inserted tag should have a positive ID")
    }

    @Test
    fun insertAndGetTagById_returnsSameTag() = runTest {
        val tag = createTag(name = "Transport", colorHex = "#2196F3")
        val id = dao.insertTag(tag)
        val fetched = dao.getTagById(id)
        assertNotNull(fetched)
        assertEquals("Transport", fetched.name)
        assertEquals("#2196F3", fetched.colorHex)
    }

    @Test
    fun getTagById_nonExistent_returnsNull() = runTest {
        val fetched = dao.getTagById(999L)
        assertNull(fetched)
    }

    @Test
    fun updateTag_modifiesFields() = runTest {
        val tag = createTag(name = "Old Name")
        val id = dao.insertTag(tag)
        val updated = createTag(id = id, name = "New Name", colorHex = "#000000")
        dao.updateTag(updated)
        val fetched = dao.getTagById(id)
        assertNotNull(fetched)
        assertEquals("New Name", fetched.name)
        assertEquals("#000000", fetched.colorHex)
    }

    @Test
    fun deleteTag_removesRecord() = runTest {
        val tag = createTag()
        val id = dao.insertTag(tag)
        dao.deleteTag(tag.copy(id = id))
        val fetched = dao.getTagById(id)
        assertNull(fetched)
    }

    @Test
    fun deleteTagById_removesRecord() = runTest {
        val tag = createTag()
        val id = dao.insertTag(tag)
        dao.deleteTagById(id)
        val fetched = dao.getTagById(id)
        assertNull(fetched)
    }

    @Test
    fun observeAllTags_returnsAllInserted() = runTest {
        dao.insertTag(createTag(name = "A"))
        dao.insertTag(createTag(name = "B"))
        dao.insertTag(createTag(name = "C"))
        val all = dao.observeAllTags().first()
        assertEquals(3, all.size)
    }

    @Test
    fun observeAllTags_orderedByNameAsc() = runTest {
        dao.insertTag(createTag(name = "Zebra"))
        dao.insertTag(createTag(name = "Apple"))
        dao.insertTag(createTag(name = "Mango"))
        val all = dao.observeAllTags().first()
        assertEquals("Apple", all[0].name)
        assertEquals("Mango", all[1].name)
        assertEquals("Zebra", all[2].name)
    }

    @Test
    fun getAllTags_returnsAllInserted() = runTest {
        dao.insertTag(createTag(name = "Food"))
        dao.insertTag(createTag(name = "Rent"))
        val all = dao.getAllTags()
        assertEquals(2, all.size)
    }

    @Test
    fun getAllTags_empty_returnsEmptyList() = runTest {
        val all = dao.getAllTags()
        assertTrue(all.isEmpty())
    }

    @Test
    fun insertTag_withReplace_overwritesExistingId() = runTest {
        val tag1 = createTag(id = 100L, name = "First", colorHex = "#111111")
        val tag2 = createTag(id = 100L, name = "Second", colorHex = "#222222")
        dao.insertTag(tag1)
        dao.insertTag(tag2)
        val fetched = dao.getTagById(100L)
        assertNotNull(fetched)
        assertEquals("Second", fetched.name)
        assertEquals("#222222", fetched.colorHex)
    }
}
