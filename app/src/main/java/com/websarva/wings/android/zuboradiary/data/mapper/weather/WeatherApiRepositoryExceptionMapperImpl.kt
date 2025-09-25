package com.websarva.wings.android.zuboradiary.data.mapper.weather

import com.websarva.wings.android.zuboradiary.data.network.exception.HttpException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkConnectivityException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkOperationException
import com.websarva.wings.android.zuboradiary.data.network.exception.ResponseParsingException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NetworkConnectionException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException

internal object WeatherApiRepositoryExceptionMapperImpl : WeatherApiRepositoryExceptionMapper {
    override fun toRepositoryException(e: NetworkOperationException): RepositoryException {
        return when (e) {
            is HttpException -> NetworkConnectionException(cause = e)
            is NetworkConnectivityException -> NetworkConnectionException(cause = e)
            is ResponseParsingException -> NetworkConnectionException(cause = e)
            else -> NetworkConnectionException(cause = e)
        }
    }
}
