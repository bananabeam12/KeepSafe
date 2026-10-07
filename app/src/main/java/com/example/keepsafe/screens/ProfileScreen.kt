package com.example.keepsafe.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.AppPreferences

@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    var isFaceIdEnabled by remember { mutableStateOf(false) }

    val firstName = AppPreferences.getUserFirstName(context)
    val lastName = AppPreferences.getUserLastName(context)
    val email = AppPreferences.getUserEmail(context)

    val fullName = if (lastName.isBlank()) firstName else "$firstName $lastName"
    val handle = "@${email.substringBefore("@")}"

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

            // 1. Profile Header Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDDF2A5)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.living_room),
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
                            text = fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1C15)
                        )
                        Text(
                            text = handle,
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
                        onClick = { navController.navigate(Routes.MANAGE_PROFILE) },
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
                                modifier = Modifier.scale(0.8f)
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
                        subtitle = "Log out of your KeepSafe session",
                        onClick = {
                            AppPreferences.clearUserSession(context)
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. More Options
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {

                    ProfileMenuItem(
                        icon = Icons.Outlined.HelpOutline,
                        title = "Help & Support",
                        subtitle = "Get assistance with your app",
                        onClick = { /* Handle Help */ },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                        }
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.Info,
                        title = "About App",
                        subtitle = "Version 1.0.0",
                        onClick = { /* Handle About */ },
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

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String,
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
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailingContent()
    }
}
