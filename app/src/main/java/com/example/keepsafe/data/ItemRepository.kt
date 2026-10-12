package com.example.keepsafe.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.keepsafe.R
import com.example.keepsafe.data.local.ItemDao
import com.example.keepsafe.data.remote.KeepSafeApiService
import com.example.keepsafe.data.remote.RemoteKeepSafeItem
import retrofit2.Response
import com.example.keepsafe.viewmodel.ItemHistoryLog
import com.example.keepsafe.viewmodel.KeepSafeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Repository pattern using ItemDao and Kotlin Coroutines (Dispatchers.IO).
 * Based on Weeks 11 & 13 of the course module.
 */
class ItemRepository(
    private val context: Context,
    private val apiService: KeepSafeApiService
) {
    private val jsonFile: File
        get() = File(context.filesDir, "keepsafe_items.json")

    private val imageDir: File
        get() = File(context.filesDir, "item_images").apply { if (!exists()) mkdirs() }

    // DAO instance coordinating local data access with Coroutines
    val dao = ItemDao(
        context = context,
        loadDiskItems = { loadItemsFromDisk() },
        saveDiskItems = { items -> saveItemsToDisk(items) }
    )

    suspend fun loadItems(): List<KeepSafeItem> = dao.getAllItems()

    suspend fun saveItem(item: KeepSafeItem) = dao.insertItem(item)

    suspend fun saveItems(items: List<KeepSafeItem>) = saveItemsToDisk(items)

    private suspend fun loadItemsFromDisk(): List<KeepSafeItem> = withContext(Dispatchers.IO) {
        if (!jsonFile.exists()) {
            val emptyList = emptyList<KeepSafeItem>()
            saveItemsToDisk(emptyList)
            return@withContext emptyList
        }

        try {
            val jsonString = jsonFile.readText()
            val jsonArray = JSONArray(jsonString)
            val items = mutableListOf<KeepSafeItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val description = obj.getString("description")
                val category = obj.getString("category")
                val itemPlaceImageRes = obj.optInt("itemPlaceImageRes", 0)
                val roomSectionImageRes = obj.optInt("roomSectionImageRes", 0)
                val lastLogged = obj.optString("lastLogged", "")
                val isRetrieved = obj.optBoolean("isRetrieved", false)

                val itemPlaceImagePath = obj.optString("itemPlaceImagePath", "").ifBlank { null }
                val roomSectionImagePath = obj.optString("roomSectionImagePath", "").ifBlank { null }

                // API items without a real photo always fall back to the living room default
                // (also repairs items saved earlier with the old keys/bedroom drawable ids)
                val isApiItem = id.startsWith("api_")
                val resolvedPlaceRes =
                    if (isApiItem && itemPlaceImagePath == null) R.drawable.living_room else itemPlaceImageRes
                val resolvedRoomRes =
                    if (isApiItem && roomSectionImagePath == null) R.drawable.living_room else roomSectionImageRes

                val itemPlaceBitmap = itemPlaceImagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) BitmapFactory.decodeFile(path) else null
                }
                val roomSectionBitmap = roomSectionImagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) BitmapFactory.decodeFile(path) else null
                }

                val historyLogsArray = obj.getJSONArray("historyLogs")
                val historyLogs = mutableListOf<ItemHistoryLog>()
                for (j in 0 until historyLogsArray.length()) {
                    val logObj = historyLogsArray.getJSONObject(j)
                    historyLogs.add(
                        ItemHistoryLog(
                            action = logObj.getString("action"),
                            timestamp = logObj.getString("timestamp")
                        )
                    )
                }

                items.add(
                    KeepSafeItem(
                        id = id,
                        title = title,
                        description = description,
                        category = category,
                        itemPlaceImageRes = resolvedPlaceRes,
                        roomSectionImageRes = resolvedRoomRes,
                        lastLogged = lastLogged,
                        historyLogs = historyLogs,
                        isRetrieved = isRetrieved,
                        itemPlaceBitmap = itemPlaceBitmap,
                        roomSectionBitmap = roomSectionBitmap,
                        itemPlaceImagePath = itemPlaceImagePath,
                        roomSectionImagePath = roomSectionImagePath
                    )
                )
            }
            // Filter out old default seed items ("1", "2", "3") and remote items, and deduplicate by title
            val cleanItems = items
                .filter { it.id != "1" && it.id != "2" && it.id != "3" && !it.id.startsWith("remote_") }
                .distinctBy { it.title.trim().lowercase() }

            saveItemsToDisk(cleanItems)
            cleanItems
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun saveItemsToDisk(items: List<KeepSafeItem>) = withContext(Dispatchers.IO) {
        val jsonArray = JSONArray()

        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("description", item.description)
            obj.put("category", item.category)
            obj.put("itemPlaceImageRes", item.itemPlaceImageRes)
            obj.put("roomSectionImageRes", item.roomSectionImageRes)
            obj.put("lastLogged", item.lastLogged)
            obj.put("isRetrieved", item.isRetrieved)

            var itemPlaceImagePath = item.itemPlaceImagePath
            if (item.itemPlaceBitmap != null && (itemPlaceImagePath == null || !File(itemPlaceImagePath).exists())) {
                val imageFile = File(imageDir, "item_${item.id}_place.png")
                saveBitmapToFile(item.itemPlaceBitmap, imageFile)
                itemPlaceImagePath = imageFile.absolutePath
            }

            var roomSectionImagePath = item.roomSectionImagePath
            if (item.roomSectionBitmap != null && (roomSectionImagePath == null || !File(roomSectionImagePath).exists())) {
                val imageFile = File(imageDir, "item_${item.id}_room.png")
                saveBitmapToFile(item.roomSectionBitmap, imageFile)
                roomSectionImagePath = imageFile.absolutePath
            }

            obj.put("itemPlaceImagePath", itemPlaceImagePath ?: JSONObject.NULL)
            obj.put("roomSectionImagePath", roomSectionImagePath ?: JSONObject.NULL)

            val logsArray = JSONArray()
            for (log in item.historyLogs) {
                val logObj = JSONObject()
                logObj.put("action", log.action)
                logObj.put("timestamp", log.timestamp)
                logsArray.put(logObj)
            }
            obj.put("historyLogs", logsArray)

            jsonArray.put(obj)
        }

        jsonFile.writeText(jsonArray.toString())
    }

    /**
     * Fetches live items from MockAPI REST API via Retrofit and Coroutines,
     * checks if items are already present, and inserts any new items into local storage.
     */
    suspend fun fetchAndAddApiItems(): Resource<List<KeepSafeItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getItems()
            if (response.isSuccessful && response.body() != null) {
                val remoteList: List<RemoteKeepSafeItem> = response.body()!!
                val currentLocalItems = dao.getAllItems()

                val existingLocalIds = currentLocalItems.map { it.id }.toSet()
                val existingLocalTitles = currentLocalItems.map { it.title.trim().lowercase() }.toSet()

                val addedItems = mutableListOf<KeepSafeItem>()

                remoteList.forEach { remote ->
                    val remoteTitle = (remote.title ?: "").trim()
                    if (remoteTitle.isNotBlank()) {
                        val stableId = if (!remote.id.isNullOrBlank()) "api_${remote.id}" else "api_${remoteTitle.lowercase()}"
                        val titleLower = remoteTitle.lowercase()

                        // Check inside coroutine if item already exists locally by ID or Title
                        if (!existingLocalIds.contains(stableId) && !existingLocalTitles.contains(titleLower)) {
                            val formattedTitle = remoteTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

                            // API items have no photos, so use the living room image as the default placeholder
                            val placeDrawable = R.drawable.living_room
                            val roomDrawable = R.drawable.living_room

                            val newItem = KeepSafeItem(
                                id = stableId,
                                title = formattedTitle,
                                description = remote.description ?: "Stored item location details",
                                category = remote.category ?: "Living Room",
                                itemPlaceImageRes = placeDrawable,
                                roomSectionImageRes = roomDrawable,
                                lastLogged = remote.lastLogged ?: "Just now",
                                historyLogs = remote.historyLogs?.map {
                                    ItemHistoryLog(
                                        action = it.action ?: "Item stored",
                                        timestamp = it.timestamp ?: "Today"
                                    )
                                } ?: listOf(ItemHistoryLog("Item stored", "Today")),
                                isRetrieved = remote.isRetrieved ?: false
                            )

                            dao.insertItem(newItem)
                            addedItems.add(newItem)
                        }
                    }
                }

                Resource.Success(addedItems)
            } else {
                Resource.Error("Couldn't sync items. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error("Couldn't sync items. Please try again.")
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}