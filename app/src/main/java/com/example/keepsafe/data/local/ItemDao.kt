package com.example.keepsafe.data.local

import android.content.Context
import com.example.keepsafe.viewmodel.KeepSafeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data Access Object (DAO) working together with Kotlin Coroutines (suspend functions, Dispatchers.IO)
 * to perform local data access operations for KeepSafe.
 * Based on Weeks 11 & 13 of the course module.
 */
class ItemDao(
    private val context: Context,
    private val loadDiskItems: suspend () -> List<KeepSafeItem>,
    private val saveDiskItems: suspend (List<KeepSafeItem>) -> Unit
) {

    suspend fun getAllItems(): List<KeepSafeItem> = withContext(Dispatchers.IO) {
        loadDiskItems()
    }

    suspend fun insertItem(item: KeepSafeItem) = withContext(Dispatchers.IO) {
        val current = loadDiskItems().toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current[index] = item
        } else {
            current.add(0, item)
        }
        saveDiskItems(current)
    }

    suspend fun updateItem(item: KeepSafeItem) = withContext(Dispatchers.IO) {
        insertItem(item)
    }
}
