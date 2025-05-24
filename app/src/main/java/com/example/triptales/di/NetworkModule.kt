// In app/src/main/java/com/example/triptales/di/NetworkModule.kt

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

// 🔧 CAMBIA QUESTA SEZIONE PER IL TUO IP LOCALE
object Constants {
    const val BASE_URL = "http://172.20.10.8:8000/"  // <-- Il tuo IP locale
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

            // Aggiungi headers comuni
            request.addHeader("Accept", "application/json")
            request.addHeader("Content-Type", "application/json")

            if (token != null) {
                // Usa Token invece di JWT
                request.addHeader("Authorization", "Token $token")
                android.util.Log.d("NetworkModule", "Adding auth token to request")
            } else {
                android.util.Log.d("NetworkModule", "No token found for request")
            }

            val response = chain.proceed(request.build())

            // Log della risposta per debug
            android.util.Log.d("NetworkModule", "Response code: ${response.code}")
            if (!response.isSuccessful) {
                android.util.Log.e("NetworkModule", "Request failed: ${response.message}")
            }

            response
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        // 🔧 USA IL TUO IP LOCALE
        .baseUrl("${Constants.BASE_URL}")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

private fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}