package com.keepingstock.data.repositories

import com.keepingstock.data.entities.ContainerEntity
import kotlinx.coroutines.flow.Flow

/**
 * This code was generated with the help of the following links
 * https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?authuser=1&continue=https%3A%2F%2Fdeveloper.android.com%2Fcourses%2Fpathways%2Fandroid-basics-compose-unit-6-pathway-2%3Fauthuser%3D1%23codelab-https%3A%2F%2Fdeveloper.android.com%2Fcodelabs%2Fbasic-android-kotlin-compose-persisting-data-room#7
 * These links document the sample code that led to my code.
 */

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

interface ContainerRepository {
    /**
     * Create
     * createdDate will be autofill, user do not need to select a date.
     */
    suspend fun createContainer(
        name: String,
        description: String? = null,
        imageUri: String? = null,
        parentContainerId:Long? = null
    ): Long

    /**
     * Update
     */
    suspend fun updateContainer(container: ContainerEntity)

    /**
     * Delete
     */
    suspend fun deleteContainer(container: ContainerEntity)

    /**
     * Get container by Id
     */
    suspend fun getContainerById(containerId: Long): ContainerEntity?

    /**
     * Observe containers with no parents
     */
    suspend fun observeRootContainers(): Flow<List<ContainerEntity>>

    /**
     * Observe direct child containers
     */
    suspend fun observeChildContainers(parentContainerId: Long): Flow<List<ContainerEntity>>

    /**
     * Search child containers by name
     */
    suspend fun searchChildContainers(
        parentContainerId: Long,
        query: String
    ): Flow<List<ContainerEntity>>
}