package com.keepingstock.data.daotest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.keepingstock.data.daos.ContainerDao
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.entities.ContainerEntity
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
class ContainerDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeepingStockDatabase
    private lateinit var containerDao: ContainerDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepingStockDatabase::class.java
        ).allowMainThreadQueries().build()
        containerDao = database.containerDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createContainer(
        name: String = "test container",
        parentContainerId: Long? = null
    ) = ContainerEntity(
        name = name,
        description = "test description",
        imageUri = null,
        parentContainerId = parentContainerId,
        createdDate = Date()
    )

    /** ---------- Test insert ---------- */
    @Test
    fun insert() = runTest {
        val generatedId = containerDao.insert(createContainer("test 1"))

        val result = containerDao.getContainerById(generatedId)

        Assert.assertNotNull(result)
        Assert.assertEquals(generatedId, result?.containerId)
        Assert.assertEquals("test 1", result?.name)
    }

    /** ---------- Test update ---------- */
    @Test
    fun update() = runTest {
        val generatedId = containerDao.insert(createContainer("test 1"))

        val containerToUpdate = createContainer("test 2").copy(containerId = generatedId)
        containerDao.update(containerToUpdate)

        val result = containerDao.getContainerById(generatedId)

        Assert.assertEquals("test 2", result?.name)
    }

    /** ---------- Test delete ---------- */
    @Test
    fun delete() = runTest {
        val generatedId = containerDao.insert(createContainer())

        val containerToDelete = containerDao.getContainerById(generatedId)!!
        containerDao.delete(containerToDelete)

        val result = containerDao.getContainerById(generatedId)

        Assert.assertNull(result)
    }

    /** ---------- Test deleteById ---------- */
    @Test
    fun deleteById() = runTest {
        val generatedId = containerDao.insert(createContainer())

        containerDao.deleteById(generatedId)

        val result = containerDao.getContainerById(generatedId)

        Assert.assertNull(result)
    }

    /** ---------- Test getRootContainers ---------- */
    @Test
    fun getRootContainers() = runTest {
        containerDao.insert(createContainer("test 1", null))
        containerDao.insert(createContainer("test 2", null))
        containerDao.insert(createContainer("test 3", 1L))

        val containers = containerDao.getRootContainers().first()

        Assert.assertEquals(2, containers.size)
    }

    /** ---------- Test getChildContainers ---------- */
    @Test
    fun getChildContainers() = runTest {
        val generatedId = containerDao.insert(createContainer(
            "test 1", null))
        containerDao.insert(createContainer("test 2", generatedId))
        containerDao.insert(createContainer("test 3", generatedId))

        val containers = containerDao.getChildContainers(generatedId).first()

        Assert.assertEquals(2, containers.size)
    }

    /** ---------- Test countChildContainers ---------- */
    @Test
    fun countChildContainers() = runTest {
        val generatedId = containerDao.insert(createContainer(
            "test 1", null))
        containerDao.insert(createContainer("test 2", generatedId))
        containerDao.insert(createContainer("test 3", generatedId))

        val count = containerDao.countChildContainers(generatedId)

        Assert.assertEquals(2, count)
    }

    /** ---------- Test searchChildContainers ---------- */
    @Test
    fun searchChildContainers() = runTest {
        val generatedId = containerDao.insert(createContainer(
            "test 1", null))
        containerDao.insert(createContainer("test 2", generatedId))
        containerDao.insert(createContainer("test 3", generatedId))

        val containers = containerDao.searchChildContainers(
            generatedId, "est").first()

        Assert.assertEquals(2, containers.size)
    }
}