package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationAccessException
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.LocationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LocationRepository (private val dataSource: FusedLocationDataSource) {

    @Throws(LocationError.AccessLocation::class)
    suspend fun fetchLocation(): GeoCoordinates {
        return withContext(Dispatchers.IO) {
            try {
                dataSource.fetchCurrentLocation()
            } catch (e: FusedLocationAccessException) {
                throw LocationError.AccessLocation(e)
            }
        }
    }
}
