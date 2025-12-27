package com.example.offlinetracking.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LocationUploadDto(
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("speed")
    val speed: Float?
)

data class LocationBatchUploadDto(
    @SerializedName("locations")
    val locations: List<LocationUploadDto>
)