package com.example.keepsafe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.HomeViewModel

data class RoomCategory(val name: String, val itemCount: Int, val imageRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    // 1. Dynamic Filter State
    val availableFilters = listOf("Medicine", "Keys", "Passport") // Based on dummy data keywords
    var activeFilters by remember { mutableStateOf(setOf<String>()) }

    // Map your rooms to their drawables so we can generate cards dynamically
    val roomImageMap = mapOf(
        "Living Room" to R.drawable.living_room,
        "Bed Room" to R.drawable.bedroom,
        "Kitchen" to R.drawable.kitchen,
        "Bathroom" to R.drawable.bathroom
    )

    // 2. Filter the ViewModel items based on Search AND Chips
    val displayedItems = viewModel.allItems.filter { item ->
        val query = viewModel.searchQuery.trim()

        // Check if item matches the typed search query
        val matchesSearch = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)

        // Check if item matches any of the active filter chips
        val matchesChips = activeFilters.isEmpty() || activeFilters.any { chip ->
            item.title.contains(chip, ignoreCase = true) ||
                    item.description.contains(chip, ignoreCase = true)
        }

        matchesSearch && matchesChips
    }

    // 3. Group the matching items by their room category
    val dynamicRoomCategories = displayedItems
        .groupBy { it.category }
        .map { (roomName, items) ->
            RoomCategory(
                name = roomName,
                itemCount = items.size,
                imageRes = roomImageMap[roomName] ?: R.drawable.living_room // Fallback image
            )
        }

    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // REUSED SEARCH BAR
            KeepSafeSearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // WORKING FILTER CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableFilters) { filter ->
                    val isSelected = activeFilters.contains(filter)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeFilters = if (isSelected) activeFilters - filter else activeFilters + filter
                        },
                        label = { Text(filter, fontSize = 13.sp) },
                        trailingIcon = if (isSelected) {
                            { Icon(Icons.Outlined.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DYNAMIC CATEGORY GRID
            if (dynamicRoomCategories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No items found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dynamicRoomCategories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = {
                                navController.navigate(Routes.roomDetail(category.name))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: RoomCategory,
                 onClick: () -> Unit) {

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // Creates the tall rectangular ratio matching the mockup
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fills available top space
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${category.itemCount} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}