package com.keepingstock.data.integration

import androidx.room.withTransaction
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.repositories.ContainerRepositoryImpl
import com.keepingstock.data.repositories.ItemRepositoryImpl
import com.keepingstock.data.repositories.TagRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Provides repeatable demo data actions for debug builds.
 */
class DemoDataManager(
    private val db: KeepingStockDatabase,
    private val containerRepo: ContainerRepositoryImpl,
    private val itemRepo: ItemRepositoryImpl,
    private val tagRepo: TagRepositoryImpl,
) {
    /**
     * Inserts a new batch of demo data.
     */
    suspend fun loadDemoData() = db.withTransaction { insertDemoData() }

    /**
     * Removes all user data.
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }
    }

    /**
     * Replaces existing data with a known demo dataset.
     */
    suspend fun resetToDemoData() {
        clearAllData()
        db.withTransaction {
            insertDemoData()
        }
    }

    private suspend fun insertDemoData() {
        val house = containerRepo.createContainer(
            name = "House",
            description = "Primary demo root",
            imageUri = "demo"
        )
        val garage = containerRepo.createContainer(
            name = "Garage",
            description = "Tools and storage",
            imageUri = "demo2",
            parentContainerId = house.id
        )
        val hallCloset = containerRepo.createContainer(
            name = "Hall Closet",
            description = "Seasonal supplies and small gear",
            parentContainerId = house.id
        )

        val toolsTag = tagRepo.createTag("tools")
        val seasonalTag = tagRepo.createTag("seasonal")
        val campingTag = tagRepo.createTag("camping")

        val drill = itemRepo.createItem(
            name = "Cordless Drill",
            description = "18V driver kit",
            imageUri = "demo",
            containerId = garage.id
        )
        val lantern = itemRepo.createItem(
            name = "Camping Lantern",
            description = "Battery powered lantern",
            imageUri = "demo2",
            containerId = hallCloset.id
        )
        val tapeMeasure = itemRepo.createItem(
            name = "Tape Measure",
            description = "Loose item for unsorted view",
            imageUri = null,
            containerId = null
        )

        tagRepo.linkTagToItem(drill.id, toolsTag.id)
        tagRepo.linkTagToItem(lantern.id, seasonalTag.id)
        tagRepo.linkTagToItem(lantern.id, campingTag.id)
        tagRepo.linkTagToItem(tapeMeasure.id, toolsTag.id)
    }
}
