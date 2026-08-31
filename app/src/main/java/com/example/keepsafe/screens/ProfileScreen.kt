package com.example.keepsafe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import androidx.compose.ui.draw.scale
import com.example.keepsafe.Routes

@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    var isFaceIdEnabled by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Profile Header Card (Green)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDDF2A5) // Soft green from the mockup
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Image (Replace R.drawable.avatar with your actual image asset)
                    Image(
                        painter = painterResource(id = R.drawable.living_room), // Dummy placeholder
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Juan Dela Cruz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1C15)
                        )
                        Text(
                            text = "@juandelacruz",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4A4E3A)
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFF1B1C15),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.navigate(Routes.MANAGE_PROFILE) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Main Settings Block
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {

                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = "My Account",
                        subtitle = "Make changes to your account",
                        onClick = { /* Handle My Account */ },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Lock,
                        title = "Face ID / Touch ID",
                        subtitle = "Manage your device security",
                        onClick = { isFaceIdEnabled = !isFaceIdEnabled },
                        trailingContent = {
                            Switch(
                                checked = isFaceIdEnabled,
                                onCheckedChange = { isFaceIdEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray
                                ),
                                modifier = Modifier.scale(0.8f) // Slightly scale down to match mockup
                            )
                        }
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Shield,
                        title = "Two-Factor Authentication",
                        subtitle = "Further secure your account for safety",
                        onClick = { /* Handle 2FA */ },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    )

                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        iconTint = Color(0xFFE53935),
                        title = "Log out",
                        titleColor = Color(0xFFE53935),
                        subtitle = "Further secure your account for safety",
                        onClick = { /* Handle Log out */ },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. "More" Section
            Text(
                text = "More",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Help & Support",
                        onClick = { /* Handle Help & Support */ },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "About App",
                        onClick = { /* Handle About App */ },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable Component for List Items
// ---------------------------------------------------------------------------
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subtle circular background for the icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Texts
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing element (Chevron, Switch, Warning Icon, etc.)
        trailingContent()
    }
}   