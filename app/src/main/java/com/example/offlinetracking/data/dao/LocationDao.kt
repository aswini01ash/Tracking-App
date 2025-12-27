package com.example.offlinetracking.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.offlinetracking.data.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Query("SELECT * FROM locations WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedLocations(): List<LocationEntity>

    @Query("UPDATE locations SET isSynced = 1 WHERE id = :locationId")
    suspend fun markLocationAsSynced(locationId: Long)

    @Query("UPDATE locations SET isSynced = 1 WHERE id IN (:locationIds)")
    suspend fun markLocationsAsSynced(locationIds: List<Long>)

    @Query("SELECT COUNT(*) FROM locations WHERE isSynced = 0")
    fun getUnsyncedLocationsCount(): Flow<Int>

    @Query("DELETE FROM locations WHERE isSynced = 1 AND timestamp < :beforeTimestamp")
    suspend fun deleteOldSyncedLocations(beforeTimestamp: Long)

    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLocations(limit: Int): List<LocationEntity>
}