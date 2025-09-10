package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationAccessFailureException
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.location.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.LocationException

internal class LocationRepositoryImpl (
    private val fusedLocationDataSource: FusedLocationDataSource
) : LocationRepository {

    override suspend fun fetchCurrentLocation(): SimpleLocation {
        return try {
            fusedLocationDataSource.fetchCurrentLocation().toDomainModel()
        } catch (e: FusedLocationAccessFailureException) {
            throw LocationException(cause = e)
        }
    }
}
