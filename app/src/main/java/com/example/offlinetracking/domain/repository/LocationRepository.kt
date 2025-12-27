package com.example.offlinetracking.domain.repository

import com.example.offlinetracking.domain.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun insertLocation(location: LocationData): Long
    suspend fun getUnsyncedLocations(): List<LocationData>
    suspend fun markLocationAsSynced(locationId: Long)
    suspend fun markLocationsAsSynced(locationIds: List<Long>)
    fun getUnsyncedLocationsCount(): Flow<Int>
    suspend fun deleteOldSyncedLocations(beforeTimestamp: Long)
}