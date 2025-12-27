package com.example.offlinetracking.domain.repository

import com.example.offlinetracking.domain.LocationData

interface NetworkRepository {
    suspend fun uploadLocations(locations: List<LocationData>): Result<Unit>
    fun isNetworkAvailable(): Boolean
}