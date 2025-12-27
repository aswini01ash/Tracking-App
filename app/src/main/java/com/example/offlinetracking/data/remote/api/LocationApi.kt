package com.example.offlinetracking.data.remote.api

import com.example.offlinetracking.data.remote.dto.LocationBatchUploadDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationApi {
    @POST("api/locations/upload")
    suspend fun uploadLocations(
        @Body locations: LocationBatchUploadDto
    ): Response<Unit>
}