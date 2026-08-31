package com.example.keepsafe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.HomeViewModel
import com.example.keepsafe.viewmodel.KeepSafeItem

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    Scaffold(
        floatingActionButton = {
            HomeFloatingActionButton(navController = navController)
        },
        bottomBar = {
            HomeBottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeHeader()
                Spacer(modifier = Modifier.height(20.dp))
                SearchAndFilterSection(viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(viewModel.filteredItems) { item ->
                KeepSafeItemCard(item = item, navController = navController)
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Column {
        Text(text = "Hi Juan!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(text = "take a breath, your home is sorted", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SearchAndFilterSection(viewModel: HomeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val searchInteractionSource = remember { MutableInteractionSource() }
        val isSearchFocused by searchInteractionSource.collectIsFocusedAsState()

        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Hinted search text", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { Icon(Icons.Filled.Search, "Search Icon", tint = if (isSearchFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = CircleShape,
            interactionSource = searchInteractionSource,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        )

        val categories = listOf("Recents", "Living Room", "Bed Room", "Kitchen", "Bathroom", "Office")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = (category == viewModel.selectedCategory)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onCategorySelected(category) },
                    label = { Text(category, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = if (category == "Recents") {
                        { Icon(Icons.Outlined.Schedule, "Recents Icon", modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = Color(0xFF1B1C15),
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLeadingIconColor = Color(0xFF1B1C15)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape
                )
            }
        }
    }
}

@Composable
fun KeepSafeItemCard(item: KeepSafeItem, navController: NavHostController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { navController.navigate(Routes.itemDetail(item.id)) }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.itemPlaceImageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Text Details (Title + Location description)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HomeFloatingActionButton(navController: NavHostController) {
    FloatingActionButton(
        onClick = { navController.navigate(Routes.PUT_AWAY) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        modifier = Modifier
            .size(64.dp)
            .offset(y = (-12).dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
    ) {
        Text("📷", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun HomeBottomNavigationBar(navController: NavHostController) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.drawBehind {
            // Draw a subtle line strictly across the top edge of the navigation bar
            drawLine(
                color = borderColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Filled.Home, "Home") })
        NavigationBarItem(selected = false, onClick = { navController.navigate(Routes.SEARCH) }, icon = { Icon(Icons.Filled.Search, "Search") })
        NavigationBarItem(selected = false, onClick = { navController.navigate(Routes.HISTORY) }, icon = { Icon(Icons.Outlined.Schedule, "History") })
        NavigationBarItem(selected = false, onClick = { navController.navigate(Routes.PROFILE) }, icon = { Icon(Icons.Filled.Person, "Profile") })
    }
}