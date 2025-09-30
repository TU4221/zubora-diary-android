package com.websarva.wings.android.zuboradiary.data.mapper.weather

import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.network.exception.HttpException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkConnectivityException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkOperationException
import com.websarva.wings.android.zuboradiary.data.network.exception.ResponseParsingException
import com.websarva.wings.android.zuboradiary.domain.exception.NetworkConnectionException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal object WeatherApiRepositoryExceptionMapper
    : RepositoryExceptionMapper<NetworkOperationException> {
    override fun toDomainException(e: NetworkOperationException): DomainException {
        return when (e) {
            is HttpException -> NetworkConnectionException(cause = e)
            is NetworkConnectivityException -> NetworkConnectionException(cause = e)
            is ResponseParsingException -> NetworkConnectionException(cause = e)
            else -> NetworkConnectionException(cause = e)
        }
    }
}
