package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationProviderException
import com.websarva.wings.android.zuboradiary.data.mapper.location.LocationRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.location.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository

internal class LocationRepositoryImpl (
    private val fusedLocationDataSource: FusedLocationDataSource,
    private val locationRepositoryExceptionMapper: LocationRepositoryExceptionMapper
) : LocationRepository {

    override suspend fun fetchCurrentLocation(): SimpleLocation {
        return try {
            fusedLocationDataSource.fetchCurrentLocation().toDomainModel()
        } catch (e: LocationProviderException) {
            throw locationRepositoryExceptionMapper.toRepositoryException(e)
        }
    }
}
