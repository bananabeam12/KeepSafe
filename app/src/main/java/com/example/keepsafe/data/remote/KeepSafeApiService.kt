package com.example.keepsafe.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface KeepSafeApiService {
    @GET("todos")
    suspend fun getStarterItems(): Response<List<RemoteKeepSafeItem>>
}
