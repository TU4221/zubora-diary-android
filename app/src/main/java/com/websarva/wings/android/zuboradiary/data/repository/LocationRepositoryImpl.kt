package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.location.LocationRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.location.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.location.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import javax.inject.Inject

internal class LocationRepositoryImpl @Inject constructor(
    private val fusedLocationDataSource: FusedLocationDataSource
) : LocationRepository {

    override suspend fun fetchCurrentLocation(): SimpleLocation {
        return try {
            fusedLocationDataSource.fetchCurrentLocation().toDomainModel()
        } catch (e: Exception) {
            throw LocationRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
