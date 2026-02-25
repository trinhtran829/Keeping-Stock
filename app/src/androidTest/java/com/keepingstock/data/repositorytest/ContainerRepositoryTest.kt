package com.keepingstock.data.repositorytest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.repositories.ContainerRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ContainerRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: KeepingStockDatabase
    private lateinit var repository: ContainerRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KeepingStockDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = ContainerRepositoryImpl(database.containerDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    /** Test createContainer */
    @Test
    fun createContainer_returnsGeneratedId() = runTest {
        val container = repository.createContainer(name = "test")

        Assert.assertNotEquals(0L, container.id.value)
        Assert.assertEquals("test", container.name)
        Assert.assertNull(container.parentContainerId)
    }

    @Test
    fun createContainer_withParent() = runTest {
        val parent = repository.createContainer(name = "parent")
        val child = repository.createContainer(name = "child", parentContainerId = parent.id)

        Assert.assertEquals(parent.id.value, child.parentContainerId?.value)
    }

    /** Test updateContainer */
    fun updateContainer() = runTest {
        val original = repository.createContainer(name = "old name")
        val update = original.copy(name = "new name", description = "new description")
        repository.updateContainer(update)

        val container = repository.getContainerById(original.id)

        Assert.assertEquals("new name", container?.name)
        Assert.assertEquals("new description", container?.description)
    }

    /** Test deleteContainer */
    @Test
    fun deleteContainer() = runTest {
        val container = repository.createContainer(name = "test")
        repository.deleteContainer(container)

        val get = repository.getContainerById(container.id)

        Assert.assertNull(get)
    }

    /** Test getContainerById */
    @Test
    fun getContainerById() = runTest {
        val created = repository.createContainer(name = "parent")
        val get = repository.getContainerById(created.id)

        Assert.assertEquals(created.id.value, get?.id?.value)
    }

    /** Test observeRootContainers */
    @Test
    fun observeRootContainers() = runTest {
        val parent = repository.createContainer(name = "parent")
        repository.createContainer(name = "child", parentContainerId = parent.id)

        val roots = repository.observeRootContainers().first()

        Assert.assertEquals(1, roots.size)
        Assert.assertEquals(parent.id.value, roots[0].id.value)
    }

    /** Test observeChildContainers */
    @Test
    fun observeChildContainers() = runTest {
        val parent = repository.createContainer(name = "parent")
        repository.createContainer(name = "child1", parentContainerId = parent.id)
        repository.createContainer(name = "child2", parentContainerId = parent.id)
        repository.createContainer(name = "unrelated")

        val children = repository.observeChildContainers(parent.id).first()

        Assert.assertEquals(2, children.size)
    }

    /** Test searchChildContainers */
    @Test
    fun searchChildContainers() = runTest {
        val parent = repository.createContainer(name = "parent")
        repository.createContainer(name = "Red Box", parentContainerId = parent.id)
        repository.createContainer(name = "Blue Box", parentContainerId = parent.id)
        repository.createContainer(name = "Red Shelf")

        val results = repository.searchChildContainers(
            parent.id,
            "Red").first()

        Assert.assertEquals(1, results.size)
        Assert.assertEquals("Red Box", results[0].name)
    }

    /** Test searchContainers */
    @Test
    fun searchContainers() = runTest {
        repository.createContainer(name = "Red Box")
        repository.createContainer(name = "Blue Box")
        repository.createContainer(name = "Red Shelf")

        val results = repository.searchContainers("Red").first()

        Assert.assertEquals(2, results.size)
        Assert.assertTrue(results.all { it.name.contains("Red") })
    }
}