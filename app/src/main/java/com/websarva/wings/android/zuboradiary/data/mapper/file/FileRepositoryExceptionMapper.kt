package com.websarva.wings.android.zuboradiary.data.mapper.file

import com.websarva.wings.android.zuboradiary.data.file.exception.FileOperationException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal interface FileRepositoryExceptionMapper {
    fun toRepositoryException(e: FileOperationException): DomainException
}
