package com.websarva.wings.android.zuboradiary.data.mapper.location

import com.websarva.wings.android.zuboradiary.data.location.exception.LocationProviderException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException

internal interface LocationRepositoryExceptionMapper {
    fun toRepositoryException(e: LocationProviderException): RepositoryException
}
