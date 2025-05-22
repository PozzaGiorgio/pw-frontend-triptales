package com.example.triptales.di

import com.example.triptales.ui.auth.LoginViewModel
import com.example.triptales.ui.auth.RegisterViewModel
import com.example.triptales.ui.map.TripMapViewModel
import com.example.triptales.ui.post.CreatePostViewModel
import com.example.triptales.ui.post.PostDetailViewModel
import com.example.triptales.ui.profile.ProfileViewModel
import com.example.triptales.ui.trip.CreateTripViewModel
import com.example.triptales.ui.trip.TripDetailViewModel
import com.example.triptales.ui.trip.TripsViewModel
import com.example.triptales.util.MlKitService
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Services
    single { MlKitService() }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { TripsViewModel(get()) }
    viewModel { TripDetailViewModel(get(), get()) }
    viewModel { CreateTripViewModel(get()) }
    viewModel { CreatePostViewModel(get(), get(), androidContext()) }
    viewModel { PostDetailViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { TripMapViewModel(get()) }
}