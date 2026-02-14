package com.keepingstock.data.repositories

import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.daos.ItemWithTagsDao
import com.keepingstock.data.entities.ItemStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import kotlin.collections.emptyList

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

class ItemRepositoryImpl(
    private val itemDao: ItemDao,
    private val itemWithTagsDao: ItemWithTagsDao,
    ) : ItemRepository {
    /**
     * Create an item
     * Upon creation, depends on containerId, status and checkoutDate
     * will be filled according to the rules.
     */
    override suspend fun createItem(
        name: String,
        description: String?,
        imageUri: String?,
        containerId: ContainerId?
    ): Item {
        // placeholder: return dummy Item
        return Item(
            id = ItemId(0L),
            name = name,
            description = description,
            imageUri = imageUri,
            containerId = containerId,
            status = ItemStatus.TAKEN_OUT,
            createdDate = Date(),
            checkoutDate = null,
            tags = emptyList()
        )
    }

    /**
     * Full item update
     */
    override suspend fun updateItem(item: Item){
        // Placeholder: do nothing for now
    }

    /**
     * Update only status
     */
    override suspend fun updateItemStatus(itemId: ItemId, status: ItemStatus) {
        // Placeholder: do nothing for now
    }

    /**
     * Delete
     */
    override suspend fun deleteItem(item: Item) {
        // Placeholder: do nothing for now
    }

    /**
     * Get item by Id
     */
    override suspend fun getItemById(itemId: ItemId): Item? {
        // Placeholder: return a dummy ItemEntity
        return Item(
            id = itemId,
            name = "Placeholder Item",
            description = "Placeholder description",
            imageUri = null,
            containerId = null,
            status = ItemStatus.TAKEN_OUT,
            createdDate = Date(),
            checkoutDate = Date(),
            tags = emptyList()
        )
    }

    /**
     * Observe all items
     */
    override fun observeItem(): Flow<List<Item>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe items in a container
     */
    override fun observeItemInContainer(containerId: ContainerId): Flow<List<Item>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe items NOT in a container (unsorted)
     */
    override fun observeItemUnsorted(): Flow<List<Item>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Search items where query match item's or tag's name
     */
    override fun searchItemsByNameOrTag(query: String): Flow<List<Item>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }
}