package com.example.keepsafe.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import org.json.JSONObject
import java.util.UUID

data class ParsedVoiceData(
    val itemName: String,
    val category: String,
    val transcript: String
)

fun parseVoiceInput(spokenText: String): ParsedVoiceData {
    val cleanedText = spokenText.trim()
    if (cleanedText.isEmpty()) return ParsedVoiceData("", "", "")

    val lower = cleanedText.lowercase()

    // 1. Extract Category (Room)
    var extractedCategory = ""
    val roomCategories = listOf(
        "masters bedroom" to "Masters bedroom",
        "master's bedroom" to "Masters bedroom",
        "master bedroom" to "Masters bedroom",
        "living room" to "Living room",
        "kitchen" to "Kitchen",
        "bedroom" to "Bedroom",
        "bathroom" to "Bathroom",
        "dining room" to "Dining room",
        "office" to "Office",
        "study room" to "Office",
        "garage" to "Garage",
        "balcony" to "Balcony",
        "patio" to "Patio",
        "porch" to "Porch",
        "basement" to "Basement",
        "attic" to "Attic",
        "hallway" to "Hallway",
        "foyer" to "Hallway",
        "entryway" to "Hallway",
        "laundry room" to "Laundry room",
        "laundry" to "Laundry room",
        "storage room" to "Storage room",
        "closet" to "Closet"
    )

    for ((key, display) in roomCategories) {
        if (lower.contains(key)) {
            extractedCategory = display
            break
        }
    }

    // 2. Extract Item Name (Title)
    val leadingPatterns = listOf(
        Regex("^(?i)(i put my|i placed my|i am putting my|i'm putting my|putting my|hanging my|left my|stored my|stashed my|i left my|i stored my|my|the|a|an)\\s+"),
        Regex("^(?i)(i put|i placed|i am putting|i'm putting|putting|hanging|left|stored|stashed)\\s+")
    )

    var itemCandidate = cleanedText
    for (pattern in leadingPatterns) {
        val match = pattern.find(itemCandidate)
        if (match != null) {
            itemCandidate = itemCandidate.substring(match.range.last + 1).trim()
            break
        }
    }

    val locationPrepositions = listOf(
        " is on the ", " is on ", " is in the ", " is in ", " is inside ", " is at the ", " is at ",
        " is under ", " is above ", " is near ", " is next to ", " on the ", " on ", " in the ", " in ",
        " inside ", " at the ", " at ", " under ", " above ", " near ", " next to "
    )

    val lowerCandidate = itemCandidate.lowercase()
    var lowestIndex = -1

    for (prep in locationPrepositions) {
        val idx = lowerCandidate.indexOf(prep)
        if (idx != -1 && (lowestIndex == -1 || idx < lowestIndex)) {
            lowestIndex = idx
        }
    }

    var extractedItemName = if (lowestIndex > 0) {
        itemCandidate.substring(0, lowestIndex).trim()
    } else {
        itemCandidate
    }

    extractedItemName = extractedItemName.replace(Regex("[.,!?]$"), "").trim()

    if (extractedItemName.isNotEmpty()) {
        extractedItemName = extractedItemName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    val formattedTranscript = cleanedText.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    return ParsedVoiceData(
        itemName = extractedItemName,
        category = extractedCategory,
        transcript = formattedTranscript
    )
}

fun parseVoiceInputToJson(spokenText: String): String {
    val parsed = parseVoiceInput(spokenText)
    val json = JSONObject()
    json.put("title", parsed.itemName)
    json.put("category", parsed.category)
    json.put("description", parsed.transcript)
    return json.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PutAwayDetailsScreen(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current

    // Check if we are relocating an existing item to pre-fill data
    val existingItem = viewModel.relocatingItemId?.let { viewModel.getItemById(it) }

    // 1. Dynamic States (Pre-filled if relocating)
    var title by remember { mutableStateOf(existingItem?.title ?: "") }
    var category by remember { mutableStateOf(existingItem?.category ?: "") }
    var transcript by remember { mutableStateOf(existingItem?.description ?: "") }

    val itemBitmap = viewModel.capturedItemImage
    val roomBitmap = viewModel.capturedRoomImage

    // Voice recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val jsonResult = parseVoiceInputToJson(spokenText)
                try {
                    val jsonObject = JSONObject(jsonResult)
                    val parsedTitle = jsonObject.optString("title", "")
                    val parsedCategory = jsonObject.optString("category", "")
                    val parsedDescription = jsonObject.optString("description", "")

                    if (parsedTitle.isNotBlank()) {
                        title = parsedTitle
                    }
                    if (parsedCategory.isNotBlank()) {
                        category = parsedCategory
                    }
                    if (parsedDescription.isNotBlank()) {
                        transcript = parsedDescription
                    }
                    Toast.makeText(context, "Voice input processed!", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the item and where you placed it...")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Voice recognition is not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

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
                        onClick = {
                            title = ""
                            category = ""
                            transcript = ""
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reset Fields", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                    }

                    // Center Voice Action Mic Button (large circle, raised slightly)
                    Surface(
                        onClick = { startVoiceInput() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .offset(y = (-16).dp)
                            .size(80.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Tap to Speak",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
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
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
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
                Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = "Edit Category", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Live Transcript", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                IconButton(
                                    onClick = { startVoiceInput() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Speak to fill fields",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            // Working Clear Button
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { transcript = "" }
                            )
                        }

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

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                            Text("Tap mic button or edit any word directly", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}