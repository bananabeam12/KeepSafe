package com.example.keepsafe.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KeepSafeRetrofitClient {
    private const val BASE_URL = "https://6acce666b406cb8ec0c6a69f.mockapi.io/"

    val apiService: KeepSafeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KeepSafeApiService::class.java)
    }
}
