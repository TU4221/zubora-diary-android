package com.websarva.wings.android.zuboradiary.data.usecase.uri

import android.net.Uri
import com.websarva.wings.android.zuboradiary.data.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult2
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class ReleaseUriPermissionUseCase(
    private val uriRepository: UriRepository,
    private val diaryRepository: DiaryRepository
) {

    sealed class ReleaseUriPermissionUseCaseException(
        message: String,
        cause: Throwable? = null
    ) : UseCaseException(message, cause) {

        class FailedException(
            cause: Throwable?
        ) : ReleaseUriPermissionUseCaseException(
            "Uri権限解放に失敗しました。",
            cause
        )
    }

    suspend operator fun invoke(uri: Uri?): UseCaseResult2<Unit, ReleaseUriPermissionUseCaseException> {
        if (uri == null) return UseCaseResult2.Success(Unit)

        val existsPicturePath =
            withContext(Dispatchers.IO) {
                diaryRepository.existsPicturePath(uri)
            }
        if (existsPicturePath) return UseCaseResult2.Success(Unit)

        try {
            uriRepository.releasePersistablePermission(uri)
        } catch (e: Exception) {
            return UseCaseResult2.Error(
                ReleaseUriPermissionUseCaseException.FailedException(e)
            )
        }

        return UseCaseResult2.Success(Unit)
    }
}
