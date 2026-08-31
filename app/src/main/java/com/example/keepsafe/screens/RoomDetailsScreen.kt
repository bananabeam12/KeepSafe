package com.example.keepsafe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.viewmodel.HomeViewModel

@Composable
fun RoomDetailScreen(
    navController: NavHostController,
    viewModel: HomeViewModel,
    roomName: String
) {
    // 1. Filter items belonging to this specific room
    val roomItems = viewModel.allItems.filter {
        it.category.equals(roomName, ignoreCase = true)
    }

    // 2. Map the room name to its background image
    val roomImageRes = when (roomName.lowercase()) {
        "living room" -> R.drawable.living_room
        "bedroom", "bed room" -> R.drawable.bedroom
        "kitchen" -> R.drawable.kitchen
        "bathroom" -> R.drawable.bathroom
        else -> R.drawable.living_room // Fallback
    }

    Scaffold(
        bottomBar = {
            // Reuse your existing bottom nav
            AppBottomNavigationBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // -- TOP HALF: Background Image --
            Image(
                painter = painterResource(id = roomImageRes),
                contentDescription = "$roomName background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f) // Takes up the top 50% of the screen
                    .align(Alignment.TopCenter)
            )

            // -- OVERLAY: Back Button & Title --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = roomName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    // Offset slightly to account for the back button so it looks perfectly centered
                    modifier = Modifier.padding(end = 48.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // -- BOTTOM HALF: Rounded Item Container --
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f) // Overlaps the bottom of the image slightly
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Item Count Text (styled with primary/green color matching the mockup)
                    Text(
                        text = "${roomItems.size} item/s",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reusing your exact KeepSafeItemCard from the Home Screen!
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(roomItems) { item ->
                            KeepSafeItemCard(item = item, navController = navController)
                        }
                    }
                }
            }
        }
    }
}