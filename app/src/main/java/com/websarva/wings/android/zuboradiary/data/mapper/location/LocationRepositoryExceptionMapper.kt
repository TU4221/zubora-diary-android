package com.websarva.wings.android.zuboradiary.data.mapper.location

import com.websarva.wings.android.zuboradiary.data.location.exception.LocationProviderException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal interface LocationRepositoryExceptionMapper {
    fun toRepositoryException(e: LocationProviderException): DomainException
}
