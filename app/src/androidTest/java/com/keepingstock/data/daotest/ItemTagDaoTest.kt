package com.keepingstock.data.daotest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.keepingstock.data.daos.ContainerDao
import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.daos.ItemTagDao
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
class ItemTagDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeepingStockDatabase
    private lateinit var itemTagDao: ItemTagDao
    private lateinit var itemDao: ItemDao
    private lateinit var tagDao: TagDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepingStockDatabase::class.java
        ).allowMainThreadQueries().build()
        itemTagDao = database.itemTagDao()
        itemDao = database.itemDao()
        tagDao = database.tagDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createItem(name: String = "test item") = ItemEntity(
        name = name,
        description = "test description",
        imageUri = null,
        containerId = null,
        status = ItemStatus.TAKEN_OUT,
        createdDate = Date(),
        checkoutDate = null
    )

    private fun createTag(name: String = "test tag") = TagEntity(
        name = name
    )


    /** ---------- Test insert (link item to tag) ---------- */
    @Test
    fun insert() = runTest {
        val itemId = itemDao.insert(createItem())
        val tagId = tagDao.insert(createTag())

        itemTagDao.insert(ItemTagEntity(itemId, tagId))

        val tags = itemTagDao.getTagIdsFromItem(itemId).first()

        Assert.assertEquals(1, tags.size)
        Assert.assertEquals(tagId, tags[0])
    }

    /** ---------- Test delete (unlink item from tag)---------- */
    @Test
    fun delete() = runTest {
        val itemId = itemDao.insert(createItem())
        val tagId = tagDao.insert(createTag())

        itemTagDao.insert(ItemTagEntity(itemId, tagId))
        itemTagDao.delete(itemId, tagId)

        val tags = itemTagDao.getTagIdsFromItem(itemId).first()

        Assert.assertTrue(tags.isEmpty())
    }

    /** ---------- Test deleteAllTagsForItem ---------- */
    @Test
    fun deleteAllTagsForItem() = runTest {
        val itemId = itemDao.insert(createItem())
        val tagId1 = tagDao.insert(createTag("tag 1"))
        val tagId2 = tagDao.insert(createTag("tag 2"))

        itemTagDao.insert(ItemTagEntity(itemId, tagId1))
        itemTagDao.insert(ItemTagEntity(itemId, tagId2))

        itemTagDao.deleteAllTagsForItem(itemId)

        val tags = itemTagDao.getTagIdsFromItem(itemId).first()

        Assert.assertTrue(tags.isEmpty())
    }

    /** ---------- Test countItemsWithTag ---------- */
    @Test
    fun countItemsWithTag() = runTest {
        val itemId1 = itemDao.insert(createItem("item 1"))
        val itemId2 = itemDao.insert(createItem("item 2"))
        val tagId = tagDao.insert(createTag())

        itemTagDao.insert(ItemTagEntity(itemId1, tagId))
        itemTagDao.insert(ItemTagEntity(itemId2, tagId))

        val count = itemTagDao.countItemsWithTag(tagId)

        Assert.assertEquals(2, count)
    }

    /** ---------- Test getTagIdsFromItem ---------- */
    @Test
    fun getTagIdsFromItem() = runTest {
        val itemId = itemDao.insert(createItem())
        val tagId1 = tagDao.insert(createTag("tag 1"))
        val tagId2 = tagDao.insert(createTag("tag 2"))

        itemTagDao.insert(ItemTagEntity(itemId, tagId1))
        itemTagDao.insert(ItemTagEntity(itemId, tagId2))

        val tags = itemTagDao.getTagIdsFromItem(itemId).first()

        Assert.assertEquals(2, tags.size)
        Assert.assertTrue(tags.contains(tagId1))
        Assert.assertTrue(tags.contains(tagId2))
    }
}