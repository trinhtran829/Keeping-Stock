package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.data.daos.ContainerDao
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
        parentContainerId: ContainerId?
    ): Container {
        // Placeholder: return dummy Container
        return Container(
            id = ContainerId(0L),
            name = name,
            description = description,
            imageUri = imageUri,
            parentContainerId = parentContainerId,
            createdDate = Date()
        )
    }

    /**
     * Update
     */
    override suspend fun updateContainer(container: Container) {
        // Placeholder: do nothing for now
    }

    /**
     * Delete
     */
    override suspend fun deleteContainer(container: Container) {
        // Placeholder: do nothing for now
    }

    /**
     * Get container by Id
     */
    override suspend fun getContainerById(containerId: ContainerId): Container? {
        // Placeholder: return a dummy Container
        return Container(
            id = containerId,
            name = "Placeholder Container",
            description = "Placeholder description",
            imageUri = null,
            parentContainerId = null,
            createdDate = Date()
        )
    }

    /**
     * Observe containers with no parents
     */
    override fun observeRootContainers(): Flow<List<Container>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe direct child containers
     */
    override fun observeChildContainers(
        parentContainerId: ContainerId
    ): Flow<List<Container>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Search child containers by name
     */
    override fun searchChildContainers(
        parentContainerId: ContainerId,
        query: String
    ): Flow<List<Container>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }
}