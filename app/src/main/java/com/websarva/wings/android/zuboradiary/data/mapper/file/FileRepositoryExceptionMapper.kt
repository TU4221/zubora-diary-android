package com.websarva.wings.android.zuboradiary.data.mapper.file

import com.websarva.wings.android.zuboradiary.data.file.exception.DirectoryDeletionFailedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileAlreadyExistsException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileDeleteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileNotFoundException
import com.websarva.wings.android.zuboradiary.data.file.exception.FilePermissionDeniedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileReadException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileWriteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileInsufficientStorageException
import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceAlreadyExistsException
import com.websarva.wings.android.zuboradiary.domain.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException

internal object FileRepositoryExceptionMapper
    : RepositoryExceptionMapper {
    override fun toDomainException(e: Exception): DomainException {
        return when (e) {
            is DirectoryDeletionFailedException -> DataStorageException(cause = e)
            is FileAlreadyExistsException -> ResourceAlreadyExistsException(cause = e)
            is FileDeleteException -> DataStorageException(cause = e)
            is FileInsufficientStorageException -> InsufficientStorageException(cause = e)
            is FileNotFoundException -> ResourceNotFoundException(cause = e)
            is FilePermissionDeniedException -> PermissionException(cause = e)
            is FileReadException -> DataStorageException(cause = e)
            is FileWriteException -> DataStorageException(cause = e)
            is RuntimeException -> throw e
            else -> UnknownException(cause = e)
        }
    }
}
