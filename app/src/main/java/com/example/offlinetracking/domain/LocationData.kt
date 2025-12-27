package com.example.offlinetracking.domain

data class LocationData(
    val id: Long = 0,
    val employeeId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val speed: Float?,
    val isSynced: Boolean = false
)