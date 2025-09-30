package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal interface SettingsRepositoryExceptionMapper {
    fun toRepositoryException(e: DataStoreException): DomainException
}
