package com.keepingstock.data.daotest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.entities.ItemEntity
import com.keepingstock.data.entities.ItemStatus
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
class ItemDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeepingStockDatabase
    private lateinit var itemDao: ItemDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepingStockDatabase::class.java
        ).allowMainThreadQueries().build()
        itemDao = database.itemDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createItem(
        name: String = "test item",
        containerId: Long? = null,
        status: ItemStatus = ItemStatus.TAKEN_OUT
    ) = ItemEntity(
        name = name,
        description = "test description",
        imageUri = null,
        containerId = containerId,
        status = status,
        createdDate = Date(),
        checkoutDate = null
    )

    /** ---------- Test insert ---------- */
    @Test
    fun insert() = runTest {
        val generatedId = itemDao.insert(createItem())

        val result = itemDao.getItemById(generatedId)

        Assert.assertNotNull(result)
        Assert.assertEquals(generatedId, result?.itemId)
        Assert.assertEquals("test item", result?.name)
    }

    /** ---------- Test update ---------- */
    @Test
    fun update() = runTest {
        val generatedId = itemDao.insert(createItem())

        val itemToUpdate = createItem("updated item").copy(itemId = generatedId)
        itemDao.update(itemToUpdate)

        val result = itemDao.getItemById(generatedId)

        Assert.assertEquals("updated item", result?.name)
    }

    /** ---------- Test delete (object) ---------- */
    @Test
    fun delete() = runTest {
        val generatedId = itemDao.insert(createItem())

        val itemToDelete = itemDao.getItemById(generatedId)!!
        itemDao.delete(itemToDelete)

        val result = itemDao.getItemById(generatedId)

        Assert.assertNull(result)
    }

    /** ---------- Test deleteById ---------- */
    @Test
    fun deleteById() = runTest {
        val generatedId = itemDao.insert(createItem())

        itemDao.deleteById(generatedId)

        val result = itemDao.getItemById(generatedId)

        Assert.assertNull(result)
    }

    /** ---------- Test updateItemStatus ---------- */
    @Test
    fun updateItemStatus() = runTest {
        val generatedId = itemDao.insert(createItem(status = ItemStatus.STORED))
        val checkoutDate = Date()

        itemDao.updateItemStatus(
            itemId = generatedId,
            status = ItemStatus.TAKEN_OUT,
            checkoutDate
        )

        val result = itemDao.getItemById(generatedId)

        Assert.assertEquals(ItemStatus.TAKEN_OUT, result?.status)
        Assert.assertEquals(checkoutDate, result?.checkoutDate)
    }

    /** ---------- Test getItems ---------- */
    @Test
    fun getItems() = runTest {
        itemDao.insert(createItem("item 1"))
        itemDao.insert(createItem("item 2"))

        val items = itemDao.getItems().first()

        Assert.assertEquals(2, items.size)
    }

    /** ---------- Test getItemsInContainer ---------- */
    @Test
    fun getItemsInContainer() = runTest {
        itemDao.insert(createItem("item 1", 1))
        itemDao.insert(createItem("item 2", null))

        val items = itemDao.getItemsInContainer(1).first()

        Assert.assertEquals(1, items.size)
        Assert.assertEquals("item 1", items[0].name)
    }

    /** ---------- Test getItemsUnsorted ---------- */
    @Test
    fun getItemsUnsorted() = runTest {
        itemDao.insert(createItem("item 1", 1))
        itemDao.insert(createItem("item 2", null))

        val items = itemDao.getItemsUnsorted().first()

        Assert.assertEquals(1, items.size)
        Assert.assertEquals("item 2", items[0].name)
        Assert.assertNull(items[0].containerId)
    }

    /** ---------- Test countItemsInContainer ---------- */
    @Test
    fun countItemsInContainer() = runTest {
        itemDao.insert(createItem("item 1", 1))
        itemDao.insert(createItem("item 2", 1))
        itemDao.insert(createItem("item 3", 2))

        val count = itemDao.countItemsInContainer(1)

        Assert.assertEquals(2, count)
    }
}