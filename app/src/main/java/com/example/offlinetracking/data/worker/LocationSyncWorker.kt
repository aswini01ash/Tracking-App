package com.example.offlinetracking.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.offlinetracking.domain.repository.LocationRepository
import com.example.offlinetracking.domain.repository.NetworkRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class LocationSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val networkRepository: NetworkRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "location_sync_work"
        private const val BATCH_SIZE = 50
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!networkRepository.isNetworkAvailable()) {
                return@withContext Result.retry()
            }

            val unsyncedLocations = locationRepository.getUnsyncedLocations()

            if (unsyncedLocations.isEmpty()) {
                return@withContext Result.success()
            }

            val batches = unsyncedLocations.chunked(BATCH_SIZE)
            var allSuccessful = true

            for (batch in batches) {
                val uploadResult = networkRepository.uploadLocations(batch)

                if (uploadResult.isSuccess) {
                    val locationIds = batch.map { it.id }
                    locationRepository.markLocationsAsSynced(locationIds)
                } else {
                    allSuccessful = false
                    break
                }
            }

            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            locationRepository.deleteOldSyncedLocations(sevenDaysAgo)

            return@withContext if (allSuccessful) {
                Result.success()
            } else {
                if (runAttemptCount < MAX_RETRY_COUNT) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }

        } catch (e: Exception) {
            return@withContext if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}