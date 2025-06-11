package com.websarva.wings.android.zuboradiary.data.usecase.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.exception.TakeUriPermissionFailedException
import com.websarva.wings.android.zuboradiary.data.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult2
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository

internal class TakeUriPermissionUseCase(
    private val uriRepository: UriRepository
) {

    sealed class TakeUriPermissionUseCaseException(
        message: String,
        cause: Throwable? = null
    ) : UseCaseException(message, cause) {

        class FailedException(
            cause: Throwable?
        ) : TakeUriPermissionUseCaseException(
            "Uri権限取得に失敗しました。",
            cause
        )
    }

    operator fun invoke(uri: Uri): UseCaseResult2<Unit, TakeUriPermissionUseCaseException> {
        return try {
            uriRepository.takePersistablePermission(uri)
            UseCaseResult2.Success(Unit)
        } catch (e: Exception) {
            UseCaseResult2.Error(
                TakeUriPermissionUseCaseException.FailedException(e)
            )
        }
    }
}
