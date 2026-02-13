package com.keepingstock.data.repositories

import com.keepingstock.data.daos.ContainerDao
import com.keepingstock.data.entities.ContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

/**
 * Placeholder repository
 *
 * NOTE TO TEAM:
 * - This is a temporary implementation
 * - These function names are final and stable
 * - UI and Integration lead can start using them
 * - The real business logic will be implemented later
 *
 * This is to ensures the UI/ViewModels code can be develop while I continue to
 * work on these logic.
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
        parentContainerId:Long?
    ): Long {
        // Placeholder: return dummy value
        return 0L
    }

    /**
     * Update
     */
    override suspend fun updateContainer(container: ContainerEntity) {
        // Placeholder: do nothing for now
    }

    /**
     * Delete
     */
    override suspend fun deleteContainer(container: ContainerEntity) {
        // Placeholder: do nothing for now
    }

    /**
     * Get container by Id
     */
    override suspend fun getContainerById(containerId: Long): ContainerEntity? {
        // Placeholder: return a dummy ContainerEntity
        return ContainerEntity(
            containerId = containerId,
            name = "Placeholder Container",
            description = "Placeholder description",
            parentContainerId = null,
            imageUri = null,
            createdDate = Date()
        )
    }

    /**
     * Observe containers with no parents
     */
    override suspend fun observeRootContainers(): Flow<List<ContainerEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe direct child containers
     */
    override suspend fun observeChildContainers(
        parentContainerId: Long
    ): Flow<List<ContainerEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Search child containers by name
     */
    override suspend fun searchChildContainers(
        parentContainerId: Long,
        query: String
    ): Flow<List<ContainerEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }
}