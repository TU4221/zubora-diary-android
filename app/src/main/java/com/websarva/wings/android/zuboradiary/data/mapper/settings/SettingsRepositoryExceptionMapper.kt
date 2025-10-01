package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataNotFoundException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreReadException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreWriteException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException

internal object SettingsRepositoryExceptionMapper
    : RepositoryExceptionMapper<DataStoreException> {
    override fun toDomainException(e: DataStoreException): DomainException {
        return when (e) {
            is DataNotFoundException -> ResourceNotFoundException(cause = e)
            is DataStoreReadException -> DataStorageException(cause = e)
            is DataStoreWriteException -> DataStorageException(cause = e)
            is InsufficientStorageException -> DataStorageException(cause = e)
            else -> DataStorageException(cause = e)
        }
    }
}
