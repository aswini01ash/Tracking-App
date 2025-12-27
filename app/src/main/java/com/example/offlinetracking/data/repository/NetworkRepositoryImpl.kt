package com.example.offlinetracking.data.repository

import com.example.offlinetracking.data.remote.NetworkManager
import com.example.offlinetracking.data.remote.api.LocationApi
import com.example.offlinetracking.data.remote.dto.LocationBatchUploadDto
import com.example.offlinetracking.data.remote.dto.LocationUploadDto
import com.example.offlinetracking.domain.LocationData
import com.example.offlinetracking.domain.repository.NetworkRepository
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor(
    private val locationApi: LocationApi,
    private val networkManager: NetworkManager
) : NetworkRepository {

    override suspend fun uploadLocations(locations: List<LocationData>): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No network available"))
            }

            val dtos = locations.map { it.toDto() }
            val batchDto = LocationBatchUploadDto(dtos)

            val response = locationApi.uploadLocations(batchDto)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isNetworkAvailable(): Boolean {
        return networkManager.isNetworkAvailable()
    }

    private fun LocationData.toDto() = LocationUploadDto(
        employeeId = employeeId,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = timestamp,
        speed = speed
    )
}
