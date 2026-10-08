package com.example.keepsafe.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface KeepSafeApiService {
    @GET("todos/1")
    suspend fun getSingleTodo(): Response<RemoteTodo>

    @GET("todos")
    suspend fun getStarterItems(): Response<List<RemoteTodo>>
}
