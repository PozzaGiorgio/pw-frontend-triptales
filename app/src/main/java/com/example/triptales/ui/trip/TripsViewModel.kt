package com.example.triptales.ui.trip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.model.Trip
import com.example.triptales.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// States per la schermata Trips
sealed class TripsState {
    object Loading : TripsState()
    data class Success(val trips: List<Trip>) : TripsState()
    data class Error(val message: String) : TripsState()
}

// ViewModel per la schermata Trips
class TripsViewModel(private val tripRepository: TripRepository) : ViewModel() {

    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Loading)
    val tripsState: StateFlow<TripsState> = _tripsState

    fun getTrips() {
        viewModelScope.launch {
            _tripsState.value = TripsState.Loading
            Log.d("TripsViewModel", "Starting to fetch trips...")

            tripRepository.getTrips().fold(
                onSuccess = { trips ->
                    Log.d("TripsViewModel", "Successfully fetched ${trips.size} trips")
                    trips.forEach { trip ->
                        Log.d("TripsViewModel", "Trip: ${trip.name}, ID: ${trip.id}")
                    }
                    _tripsState.value = TripsState.Success(trips)
                },
                onFailure = { e ->
                    Log.e("TripsViewModel", "Failed to fetch trips", e)

                    val errorMessage = when (e) {
                        is HttpException -> {
                            val errorBody = e.response()?.errorBody()?.string()
                            Log.e("TripsViewModel", "HTTP ${e.code()}: $errorBody")
                            "Server error: ${e.code()} - ${e.message()}"
                        }
                        is IOException -> {
                            Log.e("TripsViewModel", "Network error: ${e.message}")
                            "Network error: Check your connection"
                        }
                        else -> {
                            Log.e("TripsViewModel", "Unknown error: ${e.message}")
                            "Error: ${e.message ?: "Unknown error occurred"}"
                        }
                    }

                    _tripsState.value = TripsState.Error(errorMessage)
                }
            )
        }
    }
}