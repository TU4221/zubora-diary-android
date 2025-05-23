package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates

internal class LocationRepository (private val dataSource: FusedLocationDataSource) {

    suspend fun fetchLocation(): GeoCoordinates? {
        return dataSource.fetchCurrentLocation()
    }
}
