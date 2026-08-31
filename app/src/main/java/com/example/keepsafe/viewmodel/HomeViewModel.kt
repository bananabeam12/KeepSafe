package com.example.keepsafe.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.keepsafe.R


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
    val historyLogs: List<ItemHistoryLog>
)

class HomeViewModel : ViewModel() {

    // 2. State tracking for selected category filter chip
    var selectedCategory by mutableStateOf("Recents")
        private set

    // 3. State tracking for the search query
    var searchQuery by mutableStateOf("")
        private set

    // 4. Sample dummy data matching your mockups (Passport Binder, Aspirins, etc.)
    private val allItems = listOf(
        KeepSafeItem(
            id = "1",
            title = "Passport Binder",
            description = "Top shelf of the bedroom closet",
            category = "Bed Room",
            itemPlaceImageRes = R.drawable.passport_binder, // Exact place close-up
            roomSectionImageRes = R.drawable.bedroom,         // Room section view
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
            itemPlaceImageRes = R.drawable.keys, // Exact place close-up
            roomSectionImageRes = R.drawable.living_room, // Room section view
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
            itemPlaceImageRes = R.drawable.medicine_cabinet, // Exact place close-up
            roomSectionImageRes = R.drawable.bathroom,     // Room section view
            lastLogged = "Today, 1 hour ago",
            historyLogs = listOf(
                ItemHistoryLog("Placed back into medicine cabinet", "Today · 08:30 AM"),
                ItemHistoryLog("Used during morning routine", "Today · 07:15 AM")
            )
        )
    )

    // 5. Computed list based on search queries and active filter chips
    val filteredItems: List<KeepSafeItem>
        get() {
            return allItems.filter { item ->
                val matchesCategory = when (selectedCategory) {
                    "Recents" -> true // Recents shows everything or recent logs
                    else -> item.category.equals(selectedCategory, ignoreCase = true)
                }
                val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                        item.description.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }

    fun onCategorySelected(category: String) {
        selectedCategory = category
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    fun getItemById(id: String): KeepSafeItem? {
        return allItems.find { it.id == id }
    }
}