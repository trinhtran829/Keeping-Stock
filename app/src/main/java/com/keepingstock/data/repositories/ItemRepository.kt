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

interface ItemRepository {
    /**
     * Create an item
     * Rule:
     * containerId == null -> status = TAKEN_OUT, checkoutDate = now
     * containerId != null -> status = STORED, checkoutDate = null
     */
    suspend fun createItem(
        name: String,
        description: String?,
        imageUri: String? = null,
        containerId: ContainerId? = null
    ): Item

    /**
     * Full item update
     * Rule:
     * containerId change to null -> status = TAKEN_OUT, checkoutDate unchanged
     * containerId change to not-null -> status = unchanged, checkoutDate = unchanged
     */
    suspend fun updateItem(item: Item)

    /**
     * Update only item's status
     * Rule:
     * STORED -> TAKEN_OUT, checkoutDate = now
     * TAKEN_OUT -> STORED, checkoutDate = null
     */
    suspend fun updateItemStatus(
        itemId: ItemId,
        status: ItemStatus
    )

    /**
     * Delete an item
     * Also removes all item-tag rows associated with the item
     */
    suspend fun deleteItem(item: Item)

    /**
     * Get item by Id
     */
    suspend fun getItemById(itemId: ItemId): Item?

    /**
     * Observe a single item by Id, update reactively
     */
    fun observeItemById(itemId: ItemId): Flow<Item?>

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

    /**
     * Search items where query match item's name
     */
    fun searchItemsByName(query: String): Flow<List<Item>>

    /**
     * Search items where query match tag's name
     */
    fun searchItemsByTagName(query: String): Flow<List<Item>>
}