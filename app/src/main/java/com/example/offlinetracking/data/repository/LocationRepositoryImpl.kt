package com.example.offlinetracking.data.repository

import com.example.offlinetracking.data.dao.LocationDao
import com.example.offlinetracking.data.entity.LocationEntity
import com.example.offlinetracking.domain.LocationData
import com.example.offlinetracking.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao
) : LocationRepository {

    override suspend fun insertLocation(location: LocationData): Long {
        return locationDao.insertLocation(location.toEntity())
    }

    override suspend fun getUnsyncedLocations(): List<LocationData> {
        return locationDao.getUnsyncedLocations().map { it.toDomain() }
    }

    override suspend fun markLocationAsSynced(locationId: Long) {
        locationDao.markLocationAsSynced(locationId)
    }

    override suspend fun markLocationsAsSynced(locationIds: List<Long>) {
        locationDao.markLocationsAsSynced(locationIds)
    }

    override fun getUnsyncedLocationsCount(): Flow<Int> {
        return locationDao.getUnsyncedLocationsCount()
    }

    override suspend fun deleteOldSyncedLocations(beforeTimestamp: Long) {
        locationDao.deleteOldSyncedLocations(beforeTimestamp)
    }

    private fun LocationData.toEntity() = LocationEntity(
        id = id,
        employeeId = employeeId,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = timestamp,
        speed = speed,
        isSynced = isSynced
    )

    private fun LocationEntity.toDomain() = LocationData(
        id = id,
        employeeId = employeeId,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = timestamp,
        speed = speed,
        isSynced = isSynced
    )
}