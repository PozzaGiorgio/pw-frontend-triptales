package com.example.triptales.di

import com.example.triptales.ui.auth.LoginViewModel
import com.example.triptales.ui.auth.RegisterViewModel
import com.example.triptales.ui.post.CreatePostViewModel
import com.example.triptales.ui.post.PostDetailViewModel
import com.example.triptales.ui.trip.TripDetailViewModel
import com.example.triptales.ui.trip.TripsViewModel  // Percorso aggiornato
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { TripsViewModel(get()) }
    // In ViewModelModule.kt
    viewModel { TripDetailViewModel(get(), get()) }
    // In ViewModelModule.kt
    viewModel { CreatePostViewModel(get(), get(), androidContext()) }
    viewModel { PostDetailViewModel(get()) }
    // Altri ViewModel...
}