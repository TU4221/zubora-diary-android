package com.websarva.wings.android.zuboradiary.data.mapper.weather

import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkOperationException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException

internal interface WeatherApiRepositoryExceptionMapper {
    fun toRepositoryException(e: NetworkOperationException): RepositoryException
}
