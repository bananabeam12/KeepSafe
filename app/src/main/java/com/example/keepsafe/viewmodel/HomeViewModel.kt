package com.example.keepsafe.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepsafe.data.ItemRepository
import com.example.keepsafe.data.Resource
import com.example.keepsafe.data.remote.KeepSafeRetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemHistoryLog(
    val action: String,      // e.g., "Set down on kitchen counter"
    val timestamp: String    // e.g., "Today · 07:52 AM"
)

data class KeepSafeItem(
    val id: String,
    val title: String,
    val description: String,         // Specific location description
    val category: String,            // Room section (e.g., "Bed Room", "Kitchen")
    val itemPlaceImageRes: Int,      // 1. Picture of the exact place/container of the item
    val roomSectionImageRes: Int,    // 2. Picture of the wider section/room of the house
    val lastLogged: String,
    val historyLogs: List<ItemHistoryLog>,
    val isRetrieved: Boolean = false,

    val itemPlaceBitmap: Bitmap? = null,
    val roomSectionBitmap: Bitmap? = null,

    val itemPlaceImagePath: String? = null,
    val roomSectionImagePath: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ItemRepository(
        context = application,
        apiService = KeepSafeRetrofitClient.apiService
    )

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedCategory by mutableStateOf("Recents")
        private set

    var searchQuery by mutableStateOf("")
        private set

    private val _allItems = mutableStateListOf<KeepSafeItem>()
    val allItems: List<KeepSafeItem> get() = _allItems

    init {
        loadSavedItems()
    }

    /**
     * Loads locally persisted items from ItemDao asynchronously using Coroutines (Dispatchers.IO).
     * Based on Weeks 11 & 13 of the course module.
     */
    private fun loadSavedItems() {
        viewModelScope.launch {
            isLoading = true
            val loadedItems = repository.dao.getAllItems()
            _allItems.clear()
            _allItems.addAll(loadedItems)
            isLoading = false
        }
    }

    /**
     * Fetches a live dummy item from JSONPlaceholder REST API via Retrofit and Coroutines,
     * and inserts it into local storage via ItemDao (`dao.insertItem()`).
     * Based on Week 14 & 15 API requirements.
     */
    fun fetchApiItem(onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val result = repository.fetchAndAddApiItem()) {
                is Resource.Success -> {
                    result.data?.let { newItem ->
                        if (_allItems.none { it.id == newItem.id }) {
                            _allItems.add(0, newItem)
                        }
                    }
                    isLoading = false
                    onResult(true, null)
                }
                is Resource.Error -> {
                    errorMessage = result.message
                    isLoading = false
                    onResult(false, result.message)
                }
                is Resource.Loading -> {
                    // no-op
                }
            }
        }
    }

    private fun persistItem(item: KeepSafeItem) {
        viewModelScope.launch {
            repository.dao.insertItem(item)
        }
    }

    val filteredItems: List<KeepSafeItem>
        get() {
            return _allItems.filter { item ->
                val matchesCategory = when (selectedCategory) {
                    "Recents" -> true
                    else -> item.category.equals(selectedCategory, ignoreCase = true)
                }
                val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                        item.description.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }

    // Temporary storage for the camera captures
    var capturedItemImage by mutableStateOf<Bitmap?>(null)
        private set

    var capturedRoomImage by mutableStateOf<Bitmap?>(null)
        private set

    var shouldAutoLaunchCamera by mutableStateOf(false)
        private set

    var relocatingItemId by mutableStateOf<String?>(null)
        private set

    // CAMERA FLOW FUNCTIONS
    fun setAutoLaunchCamera(launch: Boolean) {
        shouldAutoLaunchCamera = launch
    }
    fun startCaptureFlow() {
        clearCapturedImages()
        shouldAutoLaunchCamera = false
    }
    fun updateCapturedItemImage(bitmap: Bitmap) {
        capturedItemImage = bitmap
    }

    fun updateCapturedRoomImage(bitmap: Bitmap) {
        capturedRoomImage = bitmap
    }
    fun clearCapturedImages() {
        capturedItemImage = null
        capturedRoomImage = null
    }

    // search and filter functions
    fun onCategorySelected(category: String) {
        selectedCategory = category
    }
    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }
    fun getItemById(id: String): KeepSafeItem? {
        return _allItems.find { it.id == id }
    }

    fun retrieveItem(itemId: String) {
        val index = _allItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = _allItems[index]
            if (item.isRetrieved) return

            val currentTime = getCurrentFormattedTime()

            val updatedHistory = listOf(
                ItemHistoryLog(action = "Retrieved item for use", timestamp = currentTime)
            ) + item.historyLogs

            val updatedItem = item.copy(
                isRetrieved = true,
                lastLogged = currentTime,
                historyLogs = updatedHistory
            )
            _allItems[index] = updatedItem
            persistItem(updatedItem)
        }
    }

    fun fastPutBack(itemId: String) {
        val index = _allItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = _allItems[index]
            if (!item.isRetrieved) return

            val currentTime = getCurrentFormattedTime()

            val updatedHistory = listOf(
                ItemHistoryLog(action = "Put back in exact same location", timestamp = currentTime)
            ) + item.historyLogs

            val updatedItem = item.copy(
                isRetrieved = false,
                lastLogged = currentTime,
                historyLogs = updatedHistory
            )
            _allItems[index] = updatedItem
            persistItem(updatedItem)
        }
    }

    fun addItem(newItem: KeepSafeItem) {
        _allItems.add(0, newItem)
        persistItem(newItem)
    }

    // Relocation Flow Functions
    fun startRelocateFlow(itemId: String) {
        relocatingItemId = itemId
        clearCapturedImages()
        shouldAutoLaunchCamera = true
    }

    fun finishRelocatingItem(newDescription: String, newCategory: String) {
        val itemId = relocatingItemId ?: return
        val index = _allItems.indexOfFirst { it.id == itemId }

        if (index != -1) {
            val item = _allItems[index]
            val currentTime = getCurrentFormattedTime()

            val updatedHistory = listOf(
                ItemHistoryLog(action = "Relocated to $newCategory", timestamp = currentTime)
            ) + item.historyLogs

            val updatedItem = item.copy(
                description = newDescription,
                category = newCategory,
                itemPlaceBitmap = capturedItemImage ?: item.itemPlaceBitmap,
                roomSectionBitmap = capturedRoomImage ?: item.roomSectionBitmap,
                isRetrieved = false,
                lastLogged = currentTime,
                historyLogs = updatedHistory
            )
            _allItems[index] = updatedItem
            persistItem(updatedItem)
        }

        relocatingItemId = null
        clearCapturedImages()
    }

    fun cancelRelocation() {
        relocatingItemId = null
        clearCapturedImages()
    }

    // Helper function that generates a string like: "Sep 01, 2026 · 04:09 AM"
    fun getCurrentFormattedTime(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        return formatter.format(Date())
    }
}
