package com.example.offlinetracking.domain.usecase

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.offlinetracking.presentation.service.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartLocationTrackingUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke(employeeId: String, updateIntervalMs: Long) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_EMPLOYEE_ID, employeeId)
            putExtra(LocationTrackingService.EXTRA_UPDATE_INTERVAL, updateIntervalMs)
        }
        context.startForegroundService(intent)
    }
}