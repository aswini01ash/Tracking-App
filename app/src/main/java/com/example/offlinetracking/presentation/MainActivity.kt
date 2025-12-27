package com.example.offlinetracking.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.offlinetracking.databinding.ActivityMainBinding
import com.example.offlinetracking.presentation.viewmodel.TrackingUiState
import com.example.offlinetracking.presentation.viewmodel.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TrackingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startTracking()
        } else {
            Toast.makeText(
                this,
                "Location permissions are required for tracking",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        binding.apply {
            btnStartTracking.setOnClickListener {
                if (checkLocationPermissions()) {
                    val employeeId = etEmployeeId.text.toString().trim()
                    val interval = etUpdateInterval.text.toString().toIntOrNull() ?: 10

                    if (employeeId.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please enter Employee ID",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    viewModel.updateEmployeeId(employeeId)
                    viewModel.updateInterval(interval)
                    viewModel.startTracking()
                } else {
                    requestLocationPermissions()
                }
            }

            btnStopTracking.setOnClickListener {
                viewModel.stopTracking()
            }

            btnManualSync.setOnClickListener {
                viewModel.manualSync()
                Toast.makeText(
                    this@MainActivity,
                    "Sync requested",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: TrackingUiState) {
        binding.apply {
            btnStartTracking.isEnabled = !state.isTracking
            btnStopTracking.isEnabled = state.isTracking
            etEmployeeId.isEnabled = !state.isTracking
            etUpdateInterval.isEnabled = !state.isTracking

            tvPendingCount.text = state.pendingLogsCount.toString()

            tvNetworkStatus.text = if (state.isOnline) "Online" else "Offline"
            tvNetworkStatus.setTextColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    if (state.isOnline) android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            tvTrackingStatus.text = if (state.isTracking) "Tracking Active" else "Tracking Stopped"
            tvTrackingStatus.setTextColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    if (state.isTracking) android.R.color.holo_green_dark
                    else android.R.color.darker_gray
                )
            )
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && coarseLocation && backgroundLocation
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        locationPermissionLauncher.launch(permissions.toTypedArray())
    }
}