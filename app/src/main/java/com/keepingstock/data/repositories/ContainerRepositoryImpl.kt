package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.data.daos.ContainerDao
import com.keepingstock.data.mapper.toDomain
import com.keepingstock.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * This code was generated with the help of the following links
 * https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#7
 * These links document the sample code that led to my code.
 */

class ContainerRepositoryImpl(private val containerDao: ContainerDao) : ContainerRepository {
    /**
     * Create
     * createdDate will be autofill, user do not need to select a date.
     */
    override suspend fun createContainer(
        name: String,
        description: String?,
        imageUri: String?,
        parentContainerId: ContainerId?
    ): Container {
        // Placeholder: return dummy Container
        val container = Container(
            id = ContainerId(0L),
            name = name,
            description = description,
            imageUri = imageUri,
            parentContainerId = parentContainerId,
            createdDate = Date()
        )
        val generatedId = containerDao.insert(container.toEntity())
        return container.copy(id = ContainerId(generatedId))
    }

    /**
     * Update
     */
    override suspend fun updateContainer(container: Container) {
        containerDao.update(container.toEntity())
    }

    /**
     * Delete
     */
    override suspend fun deleteContainer(container: Container) {
        containerDao.delete(container.toEntity())
    }

    /**
     * Get container by Id
     */
    override suspend fun getContainerById(containerId: ContainerId): Container? {
        return containerDao.getContainerById(containerId.value)?.toDomain()
    }

    /**
     * Observe containers with no parents
     */
    override fun observeRootContainers(): Flow<List<Container>> {
        return containerDao.getRootContainers()
            .map { containerList -> containerList.map { it.toDomain() } }
    }

    /**
     * Observe direct child containers
     */
    override fun observeChildContainers(
        parentContainerId: ContainerId
    ): Flow<List<Container>> {
        return containerDao.getChildContainers(parentContainerId.value)
            .map { containerList -> containerList.map { it.toDomain() } }
    }

    /**
     * Search child containers by name
     */
    override fun searchChildContainers(
        parentContainerId: ContainerId,
        query: String
    ): Flow<List<Container>> {
        return containerDao.searchChildContainers(parentContainerId.value, query)
            .map { containerList -> containerList.map { it.toDomain() } }
    }

    /**
     * Search containers by name
     */
    override fun searchContainers(
        query: String
    ): Flow<List<Container>> {
        return containerDao.searchContainers(query)
            .map { containerList -> containerList.map { it.toDomain() } }
    }
}