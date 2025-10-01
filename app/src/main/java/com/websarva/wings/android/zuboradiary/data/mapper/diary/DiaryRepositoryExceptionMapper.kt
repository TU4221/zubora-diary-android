package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseCorruptionException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseInitializationException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseStateException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseStorageFullException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordDeleteException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordNotFoundException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordReadException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordUpdateException
import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException

internal object DiaryRepositoryExceptionMapper
    : RepositoryExceptionMapper<DatabaseException> {
    override fun toDomainException(e: DatabaseException): DomainException {
        return when (e) {
            is DatabaseCorruptionException -> DataStorageException(cause = e)
            is DatabaseInitializationException -> DataStorageException(cause = e)
            is DatabaseStateException -> DataStorageException(cause = e)
            is DatabaseStorageFullException -> DataStorageException(cause = e)
            is RecordDeleteException -> DataStorageException(cause = e)
            is RecordNotFoundException -> ResourceNotFoundException(cause = e)
            is RecordReadException -> DataStorageException(cause = e)
            is RecordUpdateException -> DataStorageException(cause = e)
            else -> DataStorageException(cause = e)
        }
    }
}
