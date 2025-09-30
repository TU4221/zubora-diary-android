package com.websarva.wings.android.zuboradiary.data.mapper.file

import com.websarva.wings.android.zuboradiary.data.file.exception.DirectoryDeletionFailedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileAlreadyExistsException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileDeleteException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileNotFoundException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.data.file.exception.FilePermissionDeniedException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileReadException
import com.websarva.wings.android.zuboradiary.data.file.exception.FileWriteException
import com.websarva.wings.android.zuboradiary.data.file.exception.InsufficientStorageException
import com.websarva.wings.android.zuboradiary.data.file.exception.InvalidFilePathException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.PermissionException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceAlreadyExistsException

internal object FileRepositoryExceptionMapperImpl : FileRepositoryExceptionMapper {
    override fun toRepositoryException(e: FileOperationException): DomainException {
        return when (e) {
            is DirectoryDeletionFailedException -> DataStorageException(cause = e)
            is FileAlreadyExistsException -> ResourceAlreadyExistsException(cause = e)
            is FileDeleteException -> DataStorageException(cause = e)
            is FileNotFoundException -> ResourceNotFoundException(cause = e)
            is FilePermissionDeniedException -> PermissionException(cause = e)
            is FileReadException -> DataStorageException(cause = e)
            is FileWriteException -> DataStorageException(cause = e)
            is InsufficientStorageException -> DataStorageException(cause = e) // TODO:ストレージ空き不足時の対応検討
            is InvalidFilePathException -> InvalidParameterException(cause = e)
            else -> DataStorageException(cause = e) // TODO:FileOperationExceptionをシールドクラスにした方がいいか？
        }
    }
}
