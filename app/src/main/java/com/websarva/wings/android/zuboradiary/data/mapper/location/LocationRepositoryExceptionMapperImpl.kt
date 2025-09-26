package com.websarva.wings.android.zuboradiary.data.mapper.location

import com.websarva.wings.android.zuboradiary.data.location.exception.LocationAccessException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationProviderException
import com.websarva.wings.android.zuboradiary.data.location.exception.LocationUnavailableException
import com.websarva.wings.android.zuboradiary.data.location.exception.PermissionDeniedException
import com.websarva.wings.android.zuboradiary.domain.exception.LocationException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal object LocationRepositoryExceptionMapperImpl : LocationRepositoryExceptionMapper {
    override fun toRepositoryException(e: LocationProviderException): DomainException {
        return when (e) {
            is LocationAccessException -> LocationException(cause = e)
            is LocationUnavailableException -> LocationException(cause = e)
            is PermissionDeniedException -> PermissionException(cause = e)
            else -> LocationException(cause = e)
        }
    }
}
