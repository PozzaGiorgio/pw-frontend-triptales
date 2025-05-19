package com.example.triptales.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.triptales.data.model.Trip
import com.example.triptales.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

            tripRepository.getTrips().fold(
                onSuccess = { trips ->
                    _tripsState.value = TripsState.Success(trips)
                },
                onFailure = { e ->
                    _tripsState.value = TripsState.Error(e.message ?: "Failed to load trips")
                }
            )
        }
    }
}