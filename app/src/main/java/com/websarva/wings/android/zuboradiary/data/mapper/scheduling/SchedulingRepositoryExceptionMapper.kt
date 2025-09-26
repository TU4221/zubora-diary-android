package com.websarva.wings.android.zuboradiary.data.mapper.scheduling

import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerOperationException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal interface SchedulingRepositoryExceptionMapper {
    fun toRepositoryException(e: WorkerOperationException): DomainException
}
