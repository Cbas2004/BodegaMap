package com.sebas.bodegamap.network

import com.sebas.bodegamap.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = BuildConfig.API_BASE_URL

    // BODY vuelca URLs, headers y payloads completos al logcat: aceptable en
    // debug, pero un riesgo de fuga de datos si queda activo en release.
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // El backend está en Render (plan Free): tras ~15 min inactivo se duerme y
    // la primera petición puede tardar 30-50 s en "despertar". Con el timeout por
    // defecto (10 s) esa primera llamada fallaría. Ampliamos a 60 s para tolerarlo.
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}