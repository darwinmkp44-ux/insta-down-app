package com.instadown.app.data.api

import com.instadown.app.data.model.InstagramResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Retrofit Service Interface for the FastSaver API.
 */
interface FastSaverApiService {

    @GET("fetch")
    suspend fun fetchMedia(
        @Header("X-Api-Key") apiKey: String,
        @Query("url") url: String
    ): InstagramResponse

    companion object {
        private const val BASE_URL = "https://api.fastsaver.io/v1/"

        fun create(): FastSaverApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FastSaverApiService::class.java)
        }
    }
}
