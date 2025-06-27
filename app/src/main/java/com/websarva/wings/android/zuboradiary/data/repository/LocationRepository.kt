package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationAccessException
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.domain.exception.location.FetchCurrentLocationFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LocationRepository (private val dataSource: FusedLocationDataSource) {

    @Throws(FetchCurrentLocationFailedException::class)
    suspend fun fetchCurrentLocation(): GeoCoordinates {
        return withContext(Dispatchers.IO) {
            try {
                dataSource.fetchCurrentLocation()
            } catch (e: FusedLocationAccessException) {
                throw FetchCurrentLocationFailedException(e)
            }
        }
    }
}
