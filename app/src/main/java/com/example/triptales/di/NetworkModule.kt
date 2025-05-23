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

// 🔧 AGGIUNGI QUESTA SEZIONE CONSTANTS
object Constants {
    const val BASE_URL = "https://8a20-95-251-223-155.ngrok-free.app"  // <-- URL di ngrok dalla tua schermata
}

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
                // Usa Token invece di JWT
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
        // 🔧 USA CONSTANTS INVECE DI URL HARDCODED
        .baseUrl("${Constants.BASE_URL}/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}