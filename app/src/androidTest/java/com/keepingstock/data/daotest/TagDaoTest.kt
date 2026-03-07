package com.keepingstock.data.daotest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.keepingstock.data.daos.TagDao
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.entities.TagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This code was generated with help the following links
 * https://www.youtube.com/watch?v=xGbr9LOSbC0
 * These links document the sample code that led to my code.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class TagDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeepingStockDatabase
    private lateinit var tagDao: TagDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepingStockDatabase::class.java
        ).allowMainThreadQueries().build()
        tagDao = database.tagDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createTag(
        name: String = "test tag",
    ) = TagEntity(
        name = name
    )

    /** ---------- Test insert ---------- */
    @Test
    fun insert() = runTest {
        val generatedId = tagDao.insert(createTag("test 1"))

        val result = tagDao.getTagById(generatedId)

        Assert.assertNotNull(result)
        Assert.assertEquals(generatedId, result?.tagId)
        Assert.assertEquals("test 1", result?.name)
    }

    /** ---------- Test update ---------- */
    @Test
    fun update() = runTest {
        val generatedId = tagDao.insert(createTag("test 1"))

        val tagToUpdate = createTag("update tag").copy(tagId = generatedId)
        tagDao.update(tagToUpdate)

        val result = tagDao.getTagById(generatedId)

        Assert.assertEquals("update tag", result?.name)
    }

    /** ---------- Test delete ---------- */
    @Test
    fun delete() = runTest {
        val generatedId = tagDao.insert(createTag("test 1"))

        val tagToDelete = tagDao.getTagById(generatedId)!!
        tagDao.delete(tagToDelete)

        val result = tagDao.getTagById(generatedId)

        Assert.assertNull(result)
    }

    /** ---------- Test getTags ---------- */
    @Test
    fun getTags() = runTest {
        tagDao.insert(createTag("test 1"))
        tagDao.insert(createTag("test 2"))
        tagDao.insert(createTag("test 3"))

        val tags = tagDao.getTags().first()

        Assert.assertEquals(3, tags.size)
    }

    /** ---------- Test getTagByName ---------- */
    @Test
    fun getTagByName() = runTest {
        tagDao.insert(createTag("test 1"))
        tagDao.insert(createTag("test 2"))
        tagDao.insert(createTag("test 3"))

        val result = tagDao.getTagByName("test 1")

        Assert.assertEquals("test 1", result?.name)
    }

    /** ---------- Test getTagById ---------- */
    @Test
    fun getTagById() = runTest {
        val tagId1 = tagDao.insert(createTag("test 1"))
        val tagId2 = tagDao.insert(createTag("test 2"))
        val tagId3 = tagDao.insert(createTag("test 3"))

        val tag = tagDao.getTagById(tagId3)

        Assert.assertEquals("test 3", tag?.name)
        Assert.assertEquals(tagId3, tag?.tagId)
    }

    /** ---------- Test observeTagById ---------- */
    @Test
    fun observeTagById() = runTest {
        val tagId = tagDao.insert(createTag("test"))

        val tag = tagDao.observeTagById(tagId).first()

        Assert.assertEquals("test", tag?.name)
        Assert.assertEquals(tagId, tag?.tagId)
    }

    /** ---------------------------------------------------------------------------------
     * Edge Cases Tests
     * ----------------------------------------------------------------------------------
     * */

    /** ---------- Test observeTagById with non existent ID ---------- */
    @Test
    fun observeTagById_nonExistentId() = runTest {
        val result = tagDao.observeTagById(1L).first()

        Assert.assertNull(result)
    }

    /** ---------- Test getTags returns an empty list when there is no tag ---------- */
    @Test
    fun getTags_noTag() = runTest {
        val tags = tagDao.getTags().first()

        Assert.assertTrue(tags.isEmpty())
    }

    /** ---------- Test getTags returns tags in alphabetical order ---------- */
    @Test
    fun getTags_orderedAlphabetically() = runTest {
        tagDao.insert(createTag("c"))
        tagDao.insert(createTag("a"))
        tagDao.insert(createTag("b"))

        val tags = tagDao.getTags().first()

        Assert.assertEquals("a", tags[0].name)
        Assert.assertEquals("b", tags[1].name)
        Assert.assertEquals("c", tags[2].name)
    }

    /** ---------- Test searchTags returns an empty list when there is no tag ---------- */
    @Test
    fun searchTags_noTag() = runTest {
        val tags = tagDao.searchTags("test").first()

        Assert.assertTrue(tags.isEmpty())
    }

    /** ---------- Test searchTags returns an empty list when there is no match ---------- */
    @Test
    fun searchTags_noMatch() = runTest {
        tagDao.insert(createTag("c"))
        tagDao.insert(createTag("a"))
        tagDao.insert(createTag("b"))

        val tags = tagDao.searchTags("no match").first()

        Assert.assertTrue(tags.isEmpty())
    }

    /** ---------- Test searchTags returns results when there is partly match ---------- */
    @Test
    fun searchTags_partlyMatch() = runTest {
        tagDao.insert(createTag("notes"))
        tagDao.insert(createTag("pen"))
        tagDao.insert(createTag("notebook"))

        val tags = tagDao.searchTags("note").first()

        Assert.assertEquals(2, tags.size)
    }

    /** ---------- Test getTagsById returns null for non existent ID ---------- */
    @Test
    fun getTagById_nonExistentId() = runTest {
        val tag = tagDao.getTagById(1L)

        Assert.assertNull(tag)
    }

    /** ---------- Test insert throw exception when name is duplicate ---------- */
    @Test(expected = Exception::class)
    fun insert_duplicateName() = runTest {
        tagDao.insert(createTag("test"))
        tagDao.insert(createTag("test"))
    }

    /** ---------- Test update on non existent tag does nothing ---------- */
    @Test
    fun update_nonExistentTag() = runTest {
        val tag = createTag("nonExistent").copy(tagId = 1L)

        tagDao.update(tag)

        val result = tagDao.getTagById(1L)
        Assert.assertNull(result)
    }

    /** ---------- Test delete on non existent tag does nothing ---------- */
    @Test
    fun delete_nonExistentTag() = runTest {
        val tag = createTag("nonExistent").copy(tagId = 1L)

        tagDao.delete(tag)

        val result = tagDao.getTagById(1L)
        Assert.assertNull(result)
    }
}