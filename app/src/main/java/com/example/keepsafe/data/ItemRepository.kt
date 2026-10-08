package com.example.keepsafe.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.keepsafe.R
import com.example.keepsafe.data.local.ItemDao
import com.example.keepsafe.data.remote.KeepSafeApiService
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
                        itemPlaceImageRes = itemPlaceImageRes,
                        roomSectionImageRes = roomSectionImageRes,
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
            // Filter out old default seed items ("1", "2", "3") and remote items
            val cleanItems = items.filter { it.id != "1" && it.id != "2" && it.id != "3" && !it.id.startsWith("remote_") }
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
     * Fetches a live dummy item from JSONPlaceholder REST API via Retrofit and Coroutines,
     * and inserts it into local storage via ItemDao (`dao.insertItem()`).
     */
    suspend fun fetchAndAddApiItem(): Resource<KeepSafeItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSingleTodo()
            if (response.isSuccessful && response.body() != null) {
                val remote = response.body()!!
                val formattedTitle = remote.title.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val newItem = KeepSafeItem(
                    id = "api_${remote.id}_${System.currentTimeMillis()}",
                    title = "API: $formattedTitle",
                    description = "Fetched live from JSONPlaceholder REST API (/todos/1)",
                    category = "Living Room",
                    itemPlaceImageRes = R.drawable.keys,
                    roomSectionImageRes = R.drawable.living_room,
                    lastLogged = "Fetched via Retrofit API",
                    historyLogs = listOf(ItemHistoryLog("Fetched from REST API endpoint /todos/1", "Today")),
                    isRetrieved = remote.completed
                )
                dao.insertItem(newItem)
                Resource.Success(newItem)
            } else {
                Resource.Error("API error: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
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
