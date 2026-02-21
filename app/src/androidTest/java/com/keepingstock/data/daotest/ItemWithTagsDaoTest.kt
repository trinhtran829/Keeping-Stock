package com.keepingstock.data.daotest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.daos.ItemTagDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.daos.TagDao
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.entities.ItemEntity
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.data.entities.ItemTagEntity
import com.keepingstock.data.entities.TagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * This code was generated with help the following links
 * https://www.youtube.com/watch?v=xGbr9LOSbC0
 * These links document the sample code that led to my code.
 */

@RunWith(AndroidJUnit4::class)
@SmallTest
class ItemWithTagsDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeepingStockDatabase
    private lateinit var itemWithTagsDao: ItemWithTagsDao
    private lateinit var itemTagDao: ItemTagDao
    private lateinit var itemDao: ItemDao
    private lateinit var tagDao: TagDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepingStockDatabase::class.java
        ).allowMainThreadQueries().build()
        itemWithTagsDao = database.itemWithTagsDao()
        itemTagDao = database.itemTagDao()
        itemDao = database.itemDao()
        tagDao = database.tagDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createItem(
        name: String = "test item",
        containerId: Long? = null
    ) = ItemEntity(
        name = name,
        description = "test description",
        imageUri = null,
        containerId = containerId,
        status = ItemStatus.TAKEN_OUT,
        createdDate = Date(),
        checkoutDate = null
    )

    private fun createTag(name: String = "test tag") = TagEntity(
        name = name
    )


    /** ---------- Test getAllItemsWithTags ---------- */
    @Test
    fun getAllItemsWithTags() = runTest {
        val itemId = itemDao.insert(createItem("item 1"))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId, tagId))

        val result = itemWithTagsDao.getAllItemsWithTags().first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
        Assert.assertEquals(1, result[0].tags.size)
        Assert.assertEquals("tag", result[0].tags[0].name)
    }

    /** ---------- Test getItemsInContainerWithTags ---------- */
    @Test
    fun getItemsInContainerWithTags() = runTest {
        val itemId1 = itemDao.insert(createItem("item 1", 1))
        val itemId2 = itemDao.insert(createItem("item 2", 2))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId1, tagId))

        val result = itemWithTagsDao.getItemsInContainerWithTags(1).first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
    }

    /** ---------- Test searchByItemOrTagName (item's name)---------- */
    @Test
    fun searchByItemOrTagName_itemName() = runTest {
        itemDao.insert(createItem("item 1"))

        val result = itemWithTagsDao.searchByItemOrTagName("tem").first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
    }

    /** ---------- Test searchByItemOrTagName (tag's name)---------- */
    @Test
    fun searchByItemOrTagName_tagName() = runTest {
        val itemId = itemDao.insert(createItem("item 1"))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId, tagId))

        val result = itemWithTagsDao.searchByItemOrTagName("ag").first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
    }

    /** ---------- Test searchByItemOrTagNameInContainer (item's name)---------- */
    @Test
    fun searchByItemOrTagNameInContainer_itemName() = runTest {
        val itemId1 = itemDao.insert(createItem("item 1", 1))
        val itemId2 = itemDao.insert(createItem("item 2", 2))

        val result = itemWithTagsDao
            .searchByItemOrTagNameInContainer(1, "tem")
            .first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
    }

    /** ---------- Test searchByItemOrTagNameInContainer (tag's name)---------- */
    @Test
    fun searchByItemOrTagNameInContainer_tagName() = runTest {
        val itemId1 = itemDao.insert(createItem("item 1", 1))
        val itemId2 = itemDao.insert(createItem("item 2", 2))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId1, tagId))

        val result = itemWithTagsDao
            .searchByItemOrTagNameInContainer(1, "ag")
            .first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
    }

    /** ---------- Test searchItemsByTag ---------- */
    @Test
    fun searchItemsByTag() = runTest {
        val itemId1 = itemDao.insert(createItem("item 1", 1))
        val itemId2 = itemDao.insert(createItem("item 2", 2))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId1, tagId))
        itemTagDao.insert(ItemTagEntity(itemId2, tagId))

        val result = itemWithTagsDao
            .searchItemsByTag(tagId)
            .first()

        Assert.assertEquals(2, result.size)
    }

    /** ---------- Test getItemsByTagInContainer ---------- */
    @Test
    fun getItemsByTagInContainer() = runTest {
        val itemId1 = itemDao.insert(createItem("item 1", 1))
        val itemId2 = itemDao.insert(createItem("item 2", 2))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId1, tagId))
        itemTagDao.insert(ItemTagEntity(itemId2, tagId))

        val result = itemWithTagsDao
            .getItemsByTagInContainer(1, tagId)
            .first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item 1", result[0].item.name)
    }
}