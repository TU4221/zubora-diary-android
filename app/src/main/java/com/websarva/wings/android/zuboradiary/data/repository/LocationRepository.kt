package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class LocationRepository (private val dataSource: FusedLocationDataSource) {

    suspend fun fetchLocation(): GeoCoordinates? {
        return withContext(Dispatchers.IO) {
            dataSource.fetchCurrentLocation()
        }
    }
}
