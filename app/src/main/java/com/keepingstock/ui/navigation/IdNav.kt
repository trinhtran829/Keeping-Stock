package com.keepingstock.ui.navigation

import android.os.Bundle
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.ItemId

// Route encoding (put IDs into routes)
fun ContainerId.toNavString(): String = this.toString()
fun ItemId.toNavString(): String = this.toString()

// Route decoding (read IDs from args)
fun Bundle.containerIdOrNull(key: String): ContainerId? =
    getString(key)?.toLongOrNull()?.let(::ContainerId)

fun Bundle.itemIdOrNull(key: String): ItemId? =
    getString(key)?.toLongOrNull()?.let(::ItemId)