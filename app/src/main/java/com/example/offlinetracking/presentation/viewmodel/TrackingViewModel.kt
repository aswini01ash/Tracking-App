package com.example.offlinetracking.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinetracking.data.remote.NetworkManager
import com.example.offlinetracking.data.worker.SyncScheduler
import com.example.offlinetracking.domain.repository.LocationRepository
import com.example.offlinetracking.domain.usecase.StartLocationTrackingUseCase
import com.example.offlinetracking.domain.usecase.StopLocationTrackingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val isTracking: Boolean = false,
    val pendingLogsCount: Int = 0,
    val isOnline: Boolean = false,
    val employeeId: String = "EMP001",
    val updateInterval: Long = 10000L
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val startLocationTracking: StartLocationTrackingUseCase,
    private val stopLocationTracking: StopLocationTrackingUseCase,
    private val locationRepository: LocationRepository,
    private val networkManager: NetworkManager,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        observePendingLogs()
        observeNetworkStatus()
    }

    private fun observePendingLogs() {
        viewModelScope.launch {
            locationRepository.getUnsyncedLocationsCount()
                .collect { count ->
                    _uiState.update { it.copy(pendingLogsCount = count) }
                }
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkManager.observeNetworkStatus()
                .collect { isOnline ->
                    _uiState.update { it.copy(isOnline = isOnline) }

                    if (isOnline && _uiState.value.pendingLogsCount > 0) {
                        syncScheduler.scheduleImmediateSync()
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startTracking() {
        val state = _uiState.value
        startLocationTracking(state.employeeId, state.updateInterval)
        syncScheduler.schedulePeriodicSync()
        _uiState.update { it.copy(isTracking = true) }
    }

    fun stopTracking() {
        stopLocationTracking()
        syncScheduler.cancelSync()
        _uiState.update { it.copy(isTracking = false) }
    }

    fun updateEmployeeId(employeeId: String) {
        _uiState.update { it.copy(employeeId = employeeId) }
    }

    fun updateInterval(intervalSeconds: Int) {
        _uiState.update { it.copy(updateInterval = intervalSeconds * 1000L) }
    }

    fun manualSync() {
        if (_uiState.value.isOnline && _uiState.value.pendingLogsCount > 0) {
            syncScheduler.scheduleImmediateSync()
        }
    }
}