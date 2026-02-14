package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
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
        containerId: ContainerId? = null
    ): Item

    /**
     * Full item update
     */
    suspend fun updateItem(item: Item)

    /**
     * Update only status
     */
    suspend fun updateItemStatus(
        itemId: ItemId,
        status: ItemStatus
    )

    /**
     * Delete
     */
    suspend fun deleteItem(item: Item)

    /**
     * Get item by Id
     */
    suspend fun getItemById(itemId: ItemId): Item?

    /**
     * Observe all items
     */
    fun observeItem(): Flow<List<Item>>

    /**
     * Observe items in a container
     */
    fun observeItemInContainer(containerId: ContainerId): Flow<List<Item>>

    /**
     * Observe items NOT in a container (unsorted)
     */
    fun observeItemUnsorted(): Flow<List<Item>>

    /**
     * Search items where query match item's or tag's name
     */
    fun searchItemsByNameOrTag(query: String): Flow<List<Item>>
}