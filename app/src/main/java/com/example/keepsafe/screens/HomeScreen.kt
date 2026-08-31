package com.example.keepsafe.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.HomeViewModel
import com.example.keepsafe.viewmodel.KeepSafeItem
import android.graphics.Bitmap
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    val roomCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.updateCapturedRoomImage(bitmap)
            navController.navigate(Routes.ADD_ITEM_DETAILS)
        }
    }

    // 2. Item Camera Launcher (Fires first)
    val itemCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.updateCapturedItemImage(bitmap)
            roomCameraLauncher.launch(null) // Make sure to pass null for TakePicturePreview
        }
    }

    // 3. Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.clearCapturedImages()
            itemCameraLauncher.launch(null)
        } else {
            // Handle permission denial
        }
    }

    LaunchedEffect(viewModel.shouldAutoLaunchCamera) {
        if (viewModel.shouldAutoLaunchCamera) {
            viewModel.setAutoLaunchCamera(false)
            viewModel.clearCapturedImages()
            itemCameraLauncher.launch(null) // Now it can find and trigger the launcher!
        }
    }
    Scaffold(
        floatingActionButton = {
            HomeFloatingActionButton(
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    )

                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        // Already have permission, go straight to camera
                        viewModel.clearCapturedImages()
                        itemCameraLauncher.launch()
                    } else {
                        // Don't have permission yet, ask for it!
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavigationBar(navController = navController)
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
        // 1. Drop in the new Search Bar component
        KeepSafeSearchBar(
            query = viewModel.searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) }
        )

        // 2. Drop in the new Category Filters component
        KeepSafeCategoryFilters(
            selectedCategory = viewModel.selectedCategory,
            onCategorySelected = { viewModel.onCategorySelected(it) }
        )
    }
}

@Composable
fun KeepSafeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchInteractionSource = remember { MutableInteractionSource() }
    val isSearchFocused by searchInteractionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("Hinted search text", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            Icon(Icons.Filled.Search, "Search Icon", tint = if (isSearchFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        },
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
        modifier = modifier.fillMaxWidth().height(56.dp)
    )
}

@Composable
fun KeepSafeCategoryFilters(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Recents", "Living Room", "Bed Room", "Kitchen", "Bathroom", "Office")

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = (category == selectedCategory)
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
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
                if (item.itemPlaceBitmap != null) {
                    // Show the real captured photo
                    Image(
                        bitmap = item.itemPlaceBitmap.asImageBitmap(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback to the hardcoded resource for your dummy data
                    Image(
                        painter = painterResource(id = item.itemPlaceImageRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
fun HomeFloatingActionButton(onClick: () -> Unit) {
    // Back to standard FloatingActionButton
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        modifier = Modifier
            .padding(bottom = 16.dp)
            .size(64.dp)
            .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = CircleShape
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add, tint = MaterialTheme.colorScheme.onPrimaryContainer,
            contentDescription = "Add New Item",
            modifier = Modifier.size(28.dp) // 24dp is standard, 28dp gives it a nice little bump

        )
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.drawBehind {
            drawLine(
                color = borderColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                if (currentRoute != Routes.HOME) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.Home, "Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.SEARCH,
            onClick = {
                if (currentRoute != Routes.SEARCH) {
                    navController.navigate(Routes.SEARCH) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.Search, "Search") }
        )
        // Add your History and Profile routes similarly...
        NavigationBarItem(
            selected = false,
            onClick = {
                if (currentRoute != Routes.HISTORY) {
                    navController.navigate(Routes.HISTORY) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Outlined.Schedule, "History") })
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = {
                if (currentRoute != Routes.PROFILE) {
                    navController.navigate(Routes.PROFILE) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.Person, "Profile") }
        )
    }
}
