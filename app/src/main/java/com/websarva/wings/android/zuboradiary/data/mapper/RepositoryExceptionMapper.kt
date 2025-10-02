package com.websarva.wings.android.zuboradiary.data.mapper

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal interface RepositoryExceptionMapper {
    fun toDomainException(e: Exception): DomainException
}
