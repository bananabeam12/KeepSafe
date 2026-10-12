package com.example.keepsafe.data.remote

data class RemoteHistoryLog(
    val action: String? = null,
    val timestamp: String? = null
)

data class RemoteKeepSafeItem(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val itemPlaceImageUrl: String? = null,
    val roomSectionImageUrl: String? = null,
    val lastLogged: String? = null,
    val isRetrieved: Boolean? = false,
    val historyLogs: List<RemoteHistoryLog>? = null
)
