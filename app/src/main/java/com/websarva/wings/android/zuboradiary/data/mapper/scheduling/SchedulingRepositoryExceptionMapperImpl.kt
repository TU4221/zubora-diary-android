package com.websarva.wings.android.zuboradiary.data.mapper.scheduling

import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerCancellationException
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerEnqueueException
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerOperationException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.SchedulingException

internal object SchedulingRepositoryExceptionMapperImpl : SchedulingRepositoryExceptionMapper {
    override fun toRepositoryException(e: WorkerOperationException): RepositoryException {
        return when (e) {
            is WorkerCancellationException -> SchedulingException(cause = e)
            is WorkerEnqueueException -> SchedulingException(cause = e)
            else -> SchedulingException(cause = e)
        }
    }
}
