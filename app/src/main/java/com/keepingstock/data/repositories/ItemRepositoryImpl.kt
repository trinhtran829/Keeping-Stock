package com.keepingstock.data.repositories

import com.keepingstock.data.daos.ItemDao
import com.keepingstock.data.entities.ItemEntity
import com.keepingstock.data.entities.ItemStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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

class ItemRepositoryImpl(private val itemDao: ItemDao) : ItemRepository {
    /**
     * Create an item
     * Upon creation, depends on containerId, status and checkoutDate
     * will be filled according to the rules.
     */
    override suspend fun createItem(
        name: String,
        description: String?,
        imageUri: String?,
        containerId:Long?
    ): Long {
        // placeholder: return dummy ID
        return 0L
    }

    /**
     * Full item update
     */
    override suspend fun updateItem(item: ItemEntity){
        // Placeholder: do nothing for now
    }

    /**
     * Update only status
     */
    override suspend fun updateItemStatus(
        itemId: Long,
        status: ItemStatus
    ) {
        // Placeholder: do nothing for now
    }

    /**
     * Observe all items
     */
    override fun observeItem(): Flow<List<ItemEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe items in a container
     */
    override fun observeItemInContainer(containerId: Long): Flow<List<ItemEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Observe items NOT in a container (unsorted)
     */
    override fun observeItemUnsorted(): Flow<List<ItemEntity>> {
        // Placeholder: return empty list
        return flowOf(emptyList())
    }

    /**
     * Delete
     */
    override suspend fun deleteItem(item: ItemEntity) {
        // Placeholder: do nothing for now
    }
}