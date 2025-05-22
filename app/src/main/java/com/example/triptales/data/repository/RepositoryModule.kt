package com.example.triptales.di

import com.example.triptales.data.repository.AuthRepository
import com.example.triptales.data.repository.PostRepository
import com.example.triptales.data.repository.TripRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthRepository(get(), androidContext()) }
    single { TripRepository(get()) }
    single { PostRepository(get()) }
}