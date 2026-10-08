package com.example.keepsafe.data.remote

data class RemoteTodo(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)
