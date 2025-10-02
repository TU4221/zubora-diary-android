package com.websarva.wings.android.zuboradiary.data.mapper.scheduling

import com.websarva.wings.android.zuboradiary.data.mapper.RepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerCancellationException
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerEnqueueException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.SchedulingException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException

internal object SchedulingRepositoryExceptionMapper
    : RepositoryExceptionMapper {
    override fun toDomainException(e: Exception): DomainException {
        return when (e) {
            is WorkerCancellationException -> SchedulingException(cause = e)
            is WorkerEnqueueException -> SchedulingException(cause = e)
            is RuntimeException -> throw e
            else -> UnknownException(cause = e)
        }
    }
}
