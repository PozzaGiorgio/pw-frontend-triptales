package com.example.triptales.di

import android.content.Context
import com.example.triptales.data.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// NetworkModule.kt
val networkModule = module {
    single { provideOkHttpClient(androidContext()) }
    single { provideRetrofit(get()) }
    single { provideApiService(get()) }
}

private fun provideOkHttpClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
            val token = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("token", null)

            if (token != null) {
                request.addHeader("Authorization", "Token $token")
            }

            chain.proceed(request.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}

private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/") // localhost for emulator
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}