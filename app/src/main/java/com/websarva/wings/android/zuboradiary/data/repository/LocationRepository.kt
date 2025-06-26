package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationAccessException
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.domain.exception.location.ObtainCurrentLocationFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LocationRepository (private val dataSource: FusedLocationDataSource) {

    @Throws(ObtainCurrentLocationFailedException::class)
    suspend fun fetchLocation(): GeoCoordinates {
        return withContext(Dispatchers.IO) {
            try {
                dataSource.fetchCurrentLocation()
            } catch (e: FusedLocationAccessException) {
                throw ObtainCurrentLocationFailedException(e)
            }
        }
    }
}
