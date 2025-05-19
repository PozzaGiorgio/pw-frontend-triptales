package com.example.triptales

import android.app.Application
import com.example.triptales.di.networkModule
import com.example.triptales.di.repositoryModule
import com.example.triptales.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TripTalesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inizializza Koin per la dependency injection
        startKoin {
            // Fornisce il context dell'applicazione a Koin
            androidContext(this@TripTalesApp)

            // Registra i moduli di dipendenza
            modules(listOf(
                networkModule,    // API e networking
                repositoryModule, // Repository per accesso ai dati
                viewModelModule   // ViewModel per la UI
            ))
        }
    }
}