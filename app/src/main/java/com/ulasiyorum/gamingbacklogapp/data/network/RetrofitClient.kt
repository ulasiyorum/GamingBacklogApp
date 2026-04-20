package com.ulasiyorum.gamingbacklogapp.data.network

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5262/"

    val apiService: GameApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(GameApiService::class.java)
    }
}