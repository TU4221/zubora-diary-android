package com.websarva.wings.android.zuboradiary.data.usecase.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.exception.TakeUriPermissionFailedException
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository

internal class TakeUriPermissionUseCase(
    private val uriRepository: UriRepository
) {
    operator fun invoke(uri: Uri): UseCaseResult<Unit> {
        return try {
            uriRepository.takePersistablePermission(uri)
            UseCaseResult.Success(Unit)
        } catch (e: Exception) {
            UseCaseResult.Error(TakeUriPermissionFailedException(e))
        }
    }
}
