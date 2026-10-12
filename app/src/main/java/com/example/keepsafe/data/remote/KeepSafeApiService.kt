package com.example.keepsafe.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface KeepSafeApiService {
    @GET("item")
    suspend fun getItems(): Response<List<RemoteKeepSafeItem>>

    @GET("item/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<RemoteKeepSafeItem>
}
