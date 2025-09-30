package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal interface DiaryRepositoryExceptionMapper {
    fun toRepositoryException(e: DatabaseException): DomainException
}
