package com.example.triptales.data.repository

import com.example.triptales.data.api.ApiService
import com.example.triptales.data.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TripRepository(private val apiService: ApiService) {
    suspend fun getTrips(): Result<List<Trip>> {
        return try {
            val trips = apiService.getTrips()
            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTrip(trip: Trip): Result<Trip> {
        return try {
            val newTrip = apiService.createTrip(trip)
            Result.success(newTrip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTripById(tripId: Int): Result<Trip> {
        return try {
            val trip = apiService.getTripById(tripId)
            Result.success(trip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinTrip(tripId: Int): Result<Unit> {
        return try {
            apiService.joinTrip(tripId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveTrip(tripId: Int): Result<Unit> {
        return try {
            apiService.leaveTrip(tripId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}