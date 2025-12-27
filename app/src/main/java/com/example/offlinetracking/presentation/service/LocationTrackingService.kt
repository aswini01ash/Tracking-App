package com.example.offlinetracking.presentation.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.offlinetracking.domain.LocationData
import com.example.offlinetracking.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var locationCallback: LocationCallback? = null

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val EXTRA_EMPLOYEE_ID = "EXTRA_EMPLOYEE_ID"
        const val EXTRA_UPDATE_INTERVAL = "EXTRA_UPDATE_INTERVAL"

        private const val DEFAULT_UPDATE_INTERVAL = 10000L
        private const val DEFAULT_FASTEST_INTERVAL = 5000L
    }

    private var employeeId: String = "EMP001"
    private var updateInterval: Long = DEFAULT_UPDATE_INTERVAL

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                employeeId = intent.getStringExtra(EXTRA_EMPLOYEE_ID) ?: "EMP001"
                updateInterval = intent.getLongExtra(EXTRA_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL)
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationUpdates()
            }

            ACTION_STOP_TRACKING -> {
                stopLocationUpdates()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            updateInterval
        ).apply {
            setMinUpdateIntervalMillis(DEFAULT_FASTEST_INTERVAL)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    serviceScope.launch {
                        val locationData = LocationData(
                            employeeId = employeeId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time,
                            speed = if (location.hasSpeed()) location.speed else null,
                            isSynced = false
                        )
                        locationRepository.insertLocation(locationData)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracking your location in the background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Collecting location data...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }
}