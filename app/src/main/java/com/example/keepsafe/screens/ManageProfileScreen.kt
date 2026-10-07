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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.viewmodel.AppPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProfileScreen(navController: NavHostController) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf(AppPreferences.getUserFirstName(context)) }
    var lastName by remember { mutableStateOf(AppPreferences.getUserLastName(context)) }
    var email by remember { mutableStateOf(AppPreferences.getUserEmail(context)) }
    var phoneNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    val fullName = if (lastName.isBlank()) firstName else "$firstName $lastName"

    fun saveChanges() {
        AppPreferences.saveUserProfile(
            context = context,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim()
        )
        navController.popBackStack()
    }

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
                painter = painterResource(id = R.drawable.living_room),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(3.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
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

            UnderlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Your email address"
            )

            UnderlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "Phone number",
                leadingIcon = {
                    Text("🇵🇭", modifier = Modifier.padding(end = 8.dp))
                }
            )

            UnderlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                placeholder = "Select your gender",
                trailingIcon = {
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Dropdown")
                }
            )

            UnderlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                placeholder = "Date of Birth",
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar")
                }
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { saveChanges() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD6EB9B))
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        readOnly = readOnly,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
            unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
