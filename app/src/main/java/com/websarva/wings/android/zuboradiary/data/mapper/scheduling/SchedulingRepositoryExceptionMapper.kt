package com.websarva.wings.android.zuboradiary.data.mapper.scheduling

import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerCancellationException
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerEnqueueException
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerOperationException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.SchedulingException

internal object SchedulingRepositoryExceptionMapper
    : RepositoryExceptionMapper<WorkerOperationException> {
    override fun toDomainException(e: WorkerOperationException): DomainException {
        return when (e) {
            is WorkerCancellationException -> SchedulingException(cause = e)
            is WorkerEnqueueException -> SchedulingException(cause = e)
            else -> SchedulingException(cause = e)
        }
    }
}
