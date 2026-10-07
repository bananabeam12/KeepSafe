package com.example.keepsafe.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.keepsafe.R
import com.example.keepsafe.viewmodel.ItemHistoryLog
import com.example.keepsafe.viewmodel.KeepSafeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Repository pattern implementation for local JSON file storage and image caching
 * using Kotlin Coroutines (Dispatchers.IO).
 */
class ItemRepository(
    private val context: Context
) {
    private val jsonFile: File
        get() = File(context.filesDir, "keepsafe_items.json")

    private val imageDir: File
        get() = File(context.filesDir, "item_images").apply { if (!exists()) mkdirs() }

    /**
     * Loads items asynchronously from local disk storage using Coroutines.
     * Filters out any previously cached remote/API items so only local items are shown.
     */
    suspend fun loadItems(): List<KeepSafeItem> = withContext(Dispatchers.IO) {
        if (!jsonFile.exists()) {
            val defaultItems = getDefaultItems()
            saveItemsInternal(defaultItems)
            return@withContext defaultItems
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
                val itemPlaceImageRes = obj.optInt("itemPlaceImageRes", R.drawable.passport_binder)
                val roomSectionImageRes = obj.optInt("roomSectionImageRes", R.drawable.bedroom)
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

            // Permanently filter out any API / remote items from local storage
            val localOnlyItems = items.filter { !it.id.startsWith("remote_") && !it.description.contains("JSONPlaceholder") }
            if (localOnlyItems.isEmpty()) {
                val defaults = getDefaultItems()
                saveItemsInternal(defaults)
                defaults
            } else {
                // Re-save without the remote items so cache is cleaned
                saveItemsInternal(localOnlyItems)
                localOnlyItems
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getDefaultItems()
        }
    }

    /**
     * Saves items asynchronously to local disk storage using Coroutines.
     */
    suspend fun saveItems(items: List<KeepSafeItem>) = withContext(Dispatchers.IO) {
        saveItemsInternal(items)
    }

    private fun saveItemsInternal(items: List<KeepSafeItem>) {
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

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultItems(): List<KeepSafeItem> {
        return listOf(
            KeepSafeItem(
                id = "1",
                title = "Passport Binder",
                description = "Top shelf of the bedroom closet",
                category = "Bed Room",
                itemPlaceImageRes = R.drawable.passport_binder,
                roomSectionImageRes = R.drawable.bedroom,
                lastLogged = "Today, 4 hours ago",
                historyLogs = listOf(
                    ItemHistoryLog("Moved from office desk to bedroom closet", "Today · 12:30 PM"),
                    ItemHistoryLog("Stored inside home office drawer", "Yesterday · 04:15 PM")
                )
            ),
            KeepSafeItem(
                id = "2",
                title = "Spare House Keys",
                description = "Hanging on the entryway key hook",
                category = "Living Room",
                itemPlaceImageRes = R.drawable.keys,
                roomSectionImageRes = R.drawable.living_room,
                lastLogged = "Today, 2 hours ago",
                historyLogs = listOf(
                    ItemHistoryLog("Hung on entryway key hook", "Today · 07:52 AM"),
                    ItemHistoryLog("Left on kitchen counter", "Yesterday · 09:10 PM")
                )
            ),
            KeepSafeItem(
                id = "3",
                title = "First Aid & Daily Medications",
                description = "Top shelf of the bathroom medicine cabinet",
                category = "Bathroom",
                itemPlaceImageRes = R.drawable.medicine_cabinet,
                roomSectionImageRes = R.drawable.bathroom,
                lastLogged = "Today, 1 hour ago",
                historyLogs = listOf(
                    ItemHistoryLog("Placed back into medicine cabinet", "Today · 08:30 AM"),
                    ItemHistoryLog("Used during morning routine", "Today · 07:15 AM")
                )
            )
        )
    }
}
