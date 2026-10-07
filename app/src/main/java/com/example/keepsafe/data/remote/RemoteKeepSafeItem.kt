package com.example.keepsafe.data.remote

data class RemoteKeepSafeItem(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)
