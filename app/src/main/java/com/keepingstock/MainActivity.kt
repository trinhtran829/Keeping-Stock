package com.keepingstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.keepingstock.data.database.KeepingStockDatabase
import com.keepingstock.data.repositories.ContainerRepositoryImpl
import com.keepingstock.data.repositories.ItemRepositoryImpl
import com.keepingstock.data.repositories.TagRepositoryImpl
import com.keepingstock.ui.KeepingStockApp
import com.keepingstock.ui.theme.KeepingStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = KeepingStockDatabase.getDatabase(applicationContext)

        val containerRepo = ContainerRepositoryImpl(
            containerDao = db.containerDao()
        )
        val itemRepo = ItemRepositoryImpl(
            itemDao = db.itemDao(),
            itemWithTagsDao = db.itemWithTagsDao(),
            itemTagDao = db.itemTagDao()
        )
        val tagRepo = TagRepositoryImpl(
            tagDao = db.tagDao(),
            itemTagDao = db.itemTagDao(),
            itemWithTagsDao = db.itemWithTagsDao()
        )

        setContent {
            KeepingStockTheme {
                KeepingStockApp(
                    containerRepo = containerRepo,
                    itemRepo = itemRepo,
                    tagRepo = tagRepo
                )
            }
        }
    }
}


