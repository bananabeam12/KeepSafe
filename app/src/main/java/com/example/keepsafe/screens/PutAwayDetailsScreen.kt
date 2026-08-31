package com.example.keepsafe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.HomeViewModel
import com.example.keepsafe.viewmodel.ItemHistoryLog
import com.example.keepsafe.viewmodel.KeepSafeItem
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PutAwayDetailsScreen(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    // Check if we are relocating an existing item to pre-fill data
    val existingItem = viewModel.relocatingItemId?.let { viewModel.getItemById(it) }

    // 1. Dynamic States (Pre-filled if relocating)
    var title by remember { mutableStateOf(existingItem?.title ?: "") }
    var category by remember { mutableStateOf(existingItem?.category ?: "") }
    var transcript by remember { mutableStateOf(existingItem?.description ?: "") }

    val itemBitmap = viewModel.capturedItemImage
    val roomBitmap = viewModel.capturedRoomImage

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.relocatingItemId != null) "Relocate Item" else "Add Item",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        // 2. Fix Controls to the Bottom
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* Handle retry */ },
                        modifier = Modifier.size(48.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Retry", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        val barHeights = listOf(16.dp, 28.dp, 44.dp, 28.dp, 16.dp)
                        barHeights.forEach { height ->
                            Box(modifier = Modifier.width(6.dp).height(height).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape))
                        }
                    }

                    IconButton(
                        onClick = {
                            if (viewModel.relocatingItemId != null) {
                                // RELOCATE EXISTING ITEM FLOW
                                viewModel.finishRelocatingItem(
                                    newDescription = transcript.ifBlank { "No description provided" },
                                    newCategory = category.ifBlank { "Uncategorized" }
                                )
                            } else {
                                // ADD NEW ITEM FLOW
                                val newItem = KeepSafeItem(
                                    id = UUID.randomUUID().toString(),
                                    title = title.ifBlank { "Untitled Item" },
                                    description = transcript.ifBlank { "No description provided" },
                                    category = category.ifBlank { "Uncategorized" },
                                    itemPlaceImageRes = R.drawable.keys, // Default fallback
                                    roomSectionImageRes = R.drawable.living_room, // Default fallback
                                    lastLogged = viewModel.getCurrentFormattedTime(),
                                    historyLogs = listOf(ItemHistoryLog(action = "Item stored", timestamp = viewModel.getCurrentFormattedTime())),
                                    isRetrieved = false,
                                    itemPlaceBitmap = viewModel.capturedItemImage,
                                    roomSectionBitmap = viewModel.capturedRoomImage
                                )
                                viewModel.addItem(newItem)
                            }

                            viewModel.clearCapturedImages()
                            navController.navigate(Routes.PUT_AWAY_SUCCESS) {
                                popUpTo(Routes.ADD_ITEM_DETAILS) { inclusive = true }
                            }
                        },
                        modifier = Modifier.size(48.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Only this section scrolls now
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Editable Title Row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit Title", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    decorationBox = { innerTextField -> if (title.isEmpty()) Text("Item Name...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 18.sp) else innerTextField() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Editable Category Row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Label, contentDescription = "Edit Category", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                BasicTextField(
                    value = category,
                    onValueChange = { category = it },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    decorationBox = { innerTextField -> if (category.isEmpty()) Text("Category (e.g. Living Room)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 16.sp) else innerTextField() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dual Captured Images Showcase
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Item Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        itemBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "Item Place", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                    Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        roomBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "Room Section", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                    }
                }
            }

            // 3. Detailed Context / Live Transcript Card
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Detailed Context", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    // Adaptive color matching theme
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Live Transcript", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.height(4.dp))
                                BasicTextField(
                                    value = transcript,
                                    onValueChange = { transcript = it },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (transcript.isEmpty()) {
                                            Text(
                                                text = "e.g., Hanging keys on the living room hook",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                fontSize = 14.sp
                                            )
                                        } else {
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            // 4. Working Clear Button
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp).clickable { transcript = "" }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                            Text("Edit any word if something was misheard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}