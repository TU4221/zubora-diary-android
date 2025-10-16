package com.websarva.wings.android.zuboradiary.data.mapper.location

import com.websarva.wings.android.zuboradiary.data.location.exception.InvalidLocationRequestParameterException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationAccessException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationUnavailableException
import com.websarva.wings.android.zuboradiary.data.location.exception.PermissionDeniedException
import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.domain.exception.LocationException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException

internal object LocationRepositoryExceptionMapper
    : RepositoryExceptionMapper {
    override fun toDomainException(e: Exception): DomainException {
        return when (e) {
            is InvalidLocationRequestParameterException -> InvalidParameterException(cause = e)
            is LocationAccessException -> LocationException(cause = e)
            is LocationUnavailableException -> LocationException(cause = e)
            is PermissionDeniedException -> PermissionException(cause = e)
            is RuntimeException -> throw e
            else -> UnknownException(cause = e)
        }
    }
}
