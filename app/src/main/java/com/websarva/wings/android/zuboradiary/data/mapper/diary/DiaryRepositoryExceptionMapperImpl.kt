package com.websarva.wings.android.zuboradiary.data.mapper.diary

import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseInitializationException
import com.websarva.wings.android.zuboradiary.data.database.exception.DatabaseStateException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordDeleteException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordNotFoundException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordReadException
import com.websarva.wings.android.zuboradiary.data.database.exception.RecordUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException

internal object DiaryRepositoryExceptionMapperImpl : DiaryRepositoryExceptionMapper {
    override fun toRepositoryException(e: DatabaseException): DomainException {
        return when (e) {
            is DatabaseInitializationException -> DataStorageException(cause = e)
            is DatabaseStateException -> DataStorageException(cause = e)
            is RecordDeleteException -> DataStorageException(cause = e)
            is RecordNotFoundException -> ResourceNotFoundException(cause = e)
            is RecordReadException -> DataStorageException(cause = e)
            is RecordUpdateException -> DataStorageException(cause = e)
            else -> DataStorageException(cause = e)
        }
    }
}
