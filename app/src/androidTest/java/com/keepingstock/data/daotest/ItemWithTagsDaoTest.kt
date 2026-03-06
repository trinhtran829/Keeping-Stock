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

    /** ---------- Test observeItemWithTagsById ---------- */
    @Test
    fun observeItemWithTagsById() = runTest {
        val itemId1 = itemDao.insert(createItem("item"))
        val tagId = tagDao.insert(createTag("tag"))

        itemTagDao.insert(ItemTagEntity(itemId1, tagId))

        val result = itemWithTagsDao.observeItemWithTagsById(itemId1).first()

        Assert.assertEquals("item", result?.item?.name)
        Assert.assertEquals("tag", result?.tags?.get(0)?.name)
    }

    /** ---------------------------------------------------------------------------------
     * Edge Cases Tests
     * ----------------------------------------------------------------------------------
     * */

    /** ---------- Test getAllItemsWithTags returns
     * an empty list when there is no items nor tags ---------- */
    @Test
    fun getAllItemsWithTags_noItemNorTag() = runTest {
        val result = itemWithTagsDao.getAllItemsWithTags().first()

        Assert.assertTrue(result.isEmpty())
    }

    /** ---------- Test getItemWithTagsById returns null for non existent ID ---------- */
    @Test
    fun getItemWithTagsById_nonExistentId() = runTest {
        val result = itemWithTagsDao.getItemWithTagsById(1L)

        Assert.assertNull(result)
    }

    /** ---------- Test getItemWithTagsById returns the correct item with its tags ---------- */
    @Test
    fun getItemWithTagsById_returnsCorrectItem() = runTest {
        val itemId1 = itemDao.insert(createItem("item1"))
        val itemId2 = itemDao.insert(createItem("item2"))
        val tagId1 = tagDao.insert(createTag("tag1"))
        val tagId2 = tagDao.insert(createTag("tag2"))

        itemTagDao.insert(ItemTagEntity(itemId1, tagId1))
        itemTagDao.insert(ItemTagEntity(itemId2, tagId2))

        val result = itemWithTagsDao.getItemWithTagsById(itemId1)

        Assert.assertEquals("item1", result?.item?.name)
        Assert.assertEquals(1, result?.tags?.size)
        Assert.assertEquals("tag1", result?.tags?.get(0)?.name)
    }

    /** ---------- Test getUnsortedItemsWithTags returns item with containerId = null ---------- */
    @Test
    fun getUnsortedItemsWithTags_returnsUnsortedItems() = runTest {
        itemDao.insert(createItem("item1", 1L))
        itemDao.insert(createItem("item2", null))

        val result = itemWithTagsDao.getUnsortedItemsWithTags().first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item2", result[0].item.name)
        Assert.assertNull(result[0].item.containerId)
    }

    /** ---------- Test getItemsInContainerWithTags returns
     * an empty list when container has no items ---------- */
    @Test
    fun getItemInContainerWithTags_containerHasNoItems() = runTest {
        itemDao.insert(createItem("item1", 1L))

        val result = itemWithTagsDao.getItemsInContainerWithTags(2L).first()

        Assert.assertTrue(result.isEmpty())
    }

    /** ---------- Test searchByItemOrTagName returns
     * an empty list when there is no match ---------- */
    @Test
    fun searchByItemOrTagName_noMatch() = runTest {
        itemDao.insert(createItem("item1"))
        tagDao.insert(createTag("tag1"))

        val result = itemWithTagsDao.searchByItemOrTagName("abc").first()

        Assert.assertTrue(result.isEmpty())
    }

    /** ---------- Test searchByItemOrTagName returns
     * item when there is only item name match ---------- */
    @Test
    fun searchByItemOrTagName_onlyItemNameMatch() = runTest {
        itemDao.insert(createItem("item1"))
        tagDao.insert(createTag("tag1"))

        val result = itemWithTagsDao.searchByItemOrTagName("item").first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("item1", result[0].item.name)
    }

    /** ---------- Test searchByItemOrTagName returns
     * item when there is only tag name match ---------- */
    @Test
    fun searchByItemOrTagName_onlyTagNameMatch() = runTest {
        val itemId = itemDao.insert(createItem("album"))
        val tagId = tagDao.insert(createTag("picture"))

        itemTagDao.insert(ItemTagEntity(itemId, tagId))

        val result = itemWithTagsDao.searchByItemOrTagName("pict").first()

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("album", result[0].item.name)
        Assert.assertEquals("picture", result[0].tags[0].name)
    }

    /** ---------- Test searchItemsByTag returns
     * an empty list when no item have the tag ---------- */
    @Test
    fun searchItemsByTag_noItemHaveTag() = runTest {
        val tagId = tagDao.insert(createTag("picture"))

        val result = itemWithTagsDao.searchItemsByTag(tagId).first()

        Assert.assertTrue(result.isEmpty())
    }

    /** ---------- Test getItemsByTagInContainer returns
     * an empty list when there is an item-tag link but not in the container ---------- */
    @Test
    fun getItemsByTagInContainer_noItemInContainer() = runTest {
        val itemId = itemDao.insert(createItem("album"))
        val tagId = tagDao.insert(createTag("picture"))

        itemTagDao.insert(ItemTagEntity(itemId, tagId))

        val result = itemWithTagsDao.getItemsByTagInContainer(1L, tagId).first()

        Assert.assertTrue(result.isEmpty())
    }
}