package com.example.keepsafe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.keepsafe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProfileScreen(navController: NavHostController) {
    // Local states (Ready to be swapped with ViewModel states later)
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Account Settings", fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Profile Avatar & Info
            Image(
                painter = painterResource(id = R.drawable.living_room), // Dummy Avatar
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(3.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, CircleShape) // Pinkish border from mockup
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Juan Dela Cruz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Juandelacruz@pnc.ph",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Input Fields
            UnderlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "What's your first name?"
            )

            UnderlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "And your last name?"
            )

            // Phone Number (Assuming you have a flag drawable, using a placeholder for now)
            UnderlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "Phone number",
                leadingIcon = {
                    Text("🇵🇭", modifier = Modifier.padding(end = 8.dp)) // Temporary emoji flag
                }
            )

            UnderlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                placeholder = "Select your gender",
                trailingIcon = {
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Dropdown")
                },
                readOnly = true // Prevents typing, ideal for a dropdown trigger
            )

            UnderlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                placeholder = "What is your date of birth?",
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar", modifier = Modifier.size(20.dp))
                },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Update Button
            Button(
                onClick = { /* Handle Profile Update */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDDF2A5), // Green from mockup
                    contentColor = Color(0xFF1B1C15)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Update Profile", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Reusable text field with only a bottom border
@Composable
fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        readOnly = readOnly,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier.fillMaxWidth()
    )
}