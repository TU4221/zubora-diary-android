package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataNotFoundException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreReadException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreWriteException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreInsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException

internal object SettingsRepositoryExceptionMapper
    : RepositoryExceptionMapper {
    override fun toDomainException(e: Exception): DomainException {
        return when (e) {
            is DataNotFoundException -> ResourceNotFoundException(cause = e)
            is DataStoreInsufficientStorageException -> InsufficientStorageException(cause = e)
            is DataStoreReadException -> DataStorageException(cause = e)
            is DataStoreWriteException -> DataStorageException(cause = e)
            is RuntimeException -> throw e
            else -> UnknownException(cause = e)
        }
    }
}
