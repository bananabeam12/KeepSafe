package com.example.keepsafe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.HomeViewModel

@Composable
fun ItemRetrievedScreen(
    itemId: String,
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    //fetches the item by itemId
    val item = viewModel.getItemById(itemId)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-five / Celebration Graphic
            // Note: Add your high-five graphic to res/drawable as high_five or similar asset
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // If you use an image asset, replace R.drawable.keys with your vector/image asset
                Image(
                    painter = painterResource(id = R.drawable.hands_highfive),
                    contentDescription = "Success High Five",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Title
            Text(
                text = "Item retrieved!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle dynamically showing the item name
            Text(
                text = "${item?.title ?: "Item"} has been marked as retrieved.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary Button: Keep current location
            Button(
                onClick = {
                    viewModel.fastPutBack(itemId)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Keep current location",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1B1C15)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Outlined Button: Save to history
            OutlinedButton(
                onClick = {

                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Save to history",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}