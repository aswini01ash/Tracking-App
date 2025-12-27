package com.example.offlinetracking.di

import com.example.offlinetracking.data.repository.LocationRepositoryImpl
import com.example.offlinetracking.data.repository.NetworkRepositoryImpl
import com.example.offlinetracking.domain.repository.LocationRepository
import com.example.offlinetracking.domain.repository.NetworkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        networkRepositoryImpl: NetworkRepositoryImpl
    ): NetworkRepository
}