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

    /** ---------------------------------------------------------------------------------
     * Edge Cases Tests
     * ----------------------------------------------------------------------------------
     * */

    /** ---------- Test insert item with nullable attributes ---------- */
    @Test
    fun insert_nullAttributes() = runTest {
        val generatedId = itemDao.insert(createItem())

        val result = itemDao.getItemById(generatedId)

        Assert.assertNotNull(result)
        Assert.assertNull(result?.containerId)
        Assert.assertNull(result?.imageUri)
        Assert.assertNull(result?.checkoutDate)
    }

    /** ---------- Test getItemById returns null for non existent ID ---------- */
    @Test
    fun getItemById_nonExistentId() = runTest {
        val result = itemDao.getItemById(1L)

        Assert.assertNull(result)
    }

    /** ---------- Test update non existent item does nothing ---------- */
    @Test
    fun update_nonExistentItem() = runTest {
        val item = createItem("nonExistent").copy(itemId = 1L)

        itemDao.update(item)
        val result = itemDao.getItemById(1L)

        Assert.assertNull(result)
    }

    /** ---------- Test delete non existent item does nothing ---------- */
    @Test
    fun delete_nonExistentItem() = runTest {
        val item = createItem("nonExistent").copy(itemId = 1L)

        itemDao.delete(item)
        val result = itemDao.getItemById(1L)

        Assert.assertNull(result)
    }
}