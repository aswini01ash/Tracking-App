package com.example.offlinetracking.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.offlinetracking.data.dao.LocationDao
import com.example.offlinetracking.data.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}