package com.keepingstock.data.repositories

import com.keepingstock.data.entities.ItemEntity
import com.keepingstock.data.entities.ItemStatus
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

interface ItemRepository {
    /**
     * Create an item
     * Upon creation, depends on containerId, status and checkoutDate
     * will be filled according to the rules.
     */
    suspend fun createItem(
        name: String,
        description: String?,
        imageUri: String? = null,
        containerId:Long? = null
    ): Long

    /**
     * Full item update
     */
    suspend fun updateItem(item: ItemEntity)

    /**
     * Update only status
     */
    suspend fun updateItemStatus(
        itemId: Long,
        status: ItemStatus
    )

    /**
     * Observe all items
     */
    fun observeItem(): Flow<List<ItemEntity>>

    /**
     * Observe items in a container
     */
    fun observeItemInContainer(containerId: Long): Flow<List<ItemEntity>>

    /**
     * Observe items NOT in a container (unsorted)
     */
    fun observeItemUnsorted(): Flow<List<ItemEntity>>

    /**
     * Delete
     */
    suspend fun deleteItem(item: ItemEntity)
}