package com.websarva.wings.android.zuboradiary.data.mapper.weather

import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.network.exception.HttpException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkConnectivityException
import com.websarva.wings.android.zuboradiary.data.network.exception.ResponseParsingException
import com.websarva.wings.android.zuboradiary.domain.exception.NetworkConnectionException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException

internal object WeatherInfoRepositoryExceptionMapper
    : RepositoryExceptionMapper {
    override fun toDomainException(e: Exception): DomainException {
        return when (e) {
            is HttpException -> NetworkConnectionException(cause = e)
            is NetworkConnectivityException -> NetworkConnectionException(cause = e)
            is ResponseParsingException -> NetworkConnectionException(cause = e)
            is RuntimeException -> throw e
            else -> UnknownException(cause = e)
        }
    }
}
