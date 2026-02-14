package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
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
        parentContainerId:ContainerId? = null
    ): Container

    /**
     * Update
     */
    suspend fun updateContainer(container: Container)

    /**
     * Delete
     */
    suspend fun deleteContainer(container: Container)

    /**
     * Get container by Id
     */
    suspend fun getContainerById(containerId: ContainerId): Container?

    /**
     * Observe containers with no parents
     */
    fun observeRootContainers(): Flow<List<Container>>

    /**
     * Observe direct child containers
     */
    fun observeChildContainers(parentContainerId: ContainerId): Flow<List<Container>>

    /**
     * Search child containers by name
     */
    fun searchChildContainers(
        parentContainerId: ContainerId,
        query: String
    ): Flow<List<Container>>
}