package com.example.keepsafe.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.keepsafe.R
import android.graphics.Bitmap
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
    val roomSectionBitmap: Bitmap? = null
)

class HomeViewModel : ViewModel() {

    var selectedCategory by mutableStateOf("Recents")
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Changed to mutableStateListOf so individual items can be updated at runtime
    private val _allItems = mutableStateListOf(
        KeepSafeItem(
            id = "1",
            title = "Passport Binder",
            description = "Top shelf of the bedroom closet",
            category = "Bed Room",
            itemPlaceImageRes = R.drawable.passport_binder,
            roomSectionImageRes = R.drawable.bedroom,
            lastLogged = "Today, 4 hours ago",
            historyLogs = listOf(
                ItemHistoryLog("Moved from office desk to bedroom closet", "Today · 12:30 PM"),
                ItemHistoryLog("Stored inside home office drawer", "Yesterday · 04:15 PM")
            )
        ),
        KeepSafeItem(
            id = "2",
            title = "Spare House Keys",
            description = "Hanging on the entryway key hook",
            category = "Living Room",
            itemPlaceImageRes = R.drawable.keys,
            roomSectionImageRes = R.drawable.living_room,
            lastLogged = "Today, 2 hours ago",
            historyLogs = listOf(
                ItemHistoryLog("Hung on entryway key hook", "Today · 07:52 AM"),
                ItemHistoryLog("Left on kitchen counter", "Yesterday · 09:10 PM")
            )
        ),
        KeepSafeItem(
            id = "3",
            title = "First Aid & Daily Medications",
            description = "Top shelf of the bathroom medicine cabinet",
            category = "Bathroom",
            itemPlaceImageRes = R.drawable.medicine_cabinet,
            roomSectionImageRes = R.drawable.bathroom,
            lastLogged = "Today, 1 hour ago",
            historyLogs = listOf(
                ItemHistoryLog("Placed back into medicine cabinet", "Today · 08:30 AM"),
                ItemHistoryLog("Used during morning routine", "Today · 07:15 AM")
            )
        )
    )

    val allItems: List<KeepSafeItem> get() = _allItems

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

    //CAMERA FLOW FUNCTIONS
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

    //search and filter functions
    fun onCategorySelected(category: String) {
        selectedCategory = category
    }
    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }
    fun getItemById(id: String): KeepSafeItem? {
        return _allItems.find { it.id == id }
    }

    // Added retrieveItem function to update item state and prepend history log
    fun retrieveItem(itemId: String) {
        val index = _allItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = _allItems[index]
            if (item.isRetrieved) return

            val currentTime = getCurrentFormattedTime() // <-- Grab the real time!

            val updatedHistory = listOf(
                ItemHistoryLog(action = "Retrieved item for use", timestamp = currentTime)
            ) + item.historyLogs

            _allItems[index] = item.copy(
                isRetrieved = true,
                lastLogged = currentTime, // <-- Update the last logged property
                historyLogs = updatedHistory
            )
        }
    }

    fun fastPutBack(itemId: String) {
        val index = _allItems.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = _allItems[index]
            if (!item.isRetrieved) return

            val currentTime = getCurrentFormattedTime() // <-- Grab the real time!

            val updatedHistory = listOf(
                ItemHistoryLog(action = "Put back in exact same location", timestamp = currentTime)
            ) + item.historyLogs

            _allItems[index] = item.copy(
                isRetrieved = false,
                lastLogged = currentTime, // <-- Update the last logged property
                historyLogs = updatedHistory
            )
        }
    }

    // Adds a new item to the very top of the list
    fun addItem(newItem: KeepSafeItem) {
        _allItems.add(0, newItem)
    }

    // Relocation Flow Functions
    // 1. Start the relocate flow
    fun startRelocateFlow(itemId: String) {
        relocatingItemId = itemId
        clearCapturedImages()
        shouldAutoLaunchCamera = true // Reuses your existing auto-launch trigger!
    }

    // 2. Finalize the relocate flow
    fun finishRelocatingItem(newDescription: String, newCategory: String) {
        val itemId = relocatingItemId ?: return
        val index = _allItems.indexOfFirst { it.id == itemId }

        if (index != -1) {
            val item = _allItems[index]
            val currentTime = getCurrentFormattedTime() // Using the timestamp we just built!

            val updatedHistory = listOf(
                ItemHistoryLog(action = "Relocated to $newCategory", timestamp = currentTime)
            ) + item.historyLogs

            // Update the item with new images and location data
            _allItems[index] = item.copy(
                description = newDescription,
                category = newCategory,
                itemPlaceBitmap = capturedItemImage ?: item.itemPlaceBitmap,
                roomSectionBitmap = capturedRoomImage ?: item.roomSectionBitmap,
                isRetrieved = false, // It's safely put away now
                lastLogged = currentTime,
                historyLogs = updatedHistory
            )
        }

        // Clean up after saving
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