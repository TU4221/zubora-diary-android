package com.websarva.wings.android.zuboradiary.domain.usecase.uri

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.error.ReleaseUriPermissionError
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.CheckDiaryPicturePathUsedFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.error.UriError
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ReleaseUriPermissionUseCase(
    private val uriRepository: UriRepository,
    private val diaryRepository: DiaryRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(uri: Uri?): UseCaseResult<Unit, ReleaseUriPermissionError> {
        val logMsg = "Uri権限解放_"
        Log.i(logTag, "${logMsg}開始")

        if (uri == null) return UseCaseResult.Success(Unit)

        try {
            val existsPicturePath = diaryRepository.existsPicturePath(uri)
            if (existsPicturePath) return UseCaseResult.Success(Unit)
        } catch (e: CheckDiaryPicturePathUsedFailedException) {
            val error = ReleaseUriPermissionError.CheckUriUsage(e)
            Log.e(logTag, "${logMsg}失敗", error)
            return UseCaseResult.Error(error)
        }

        try {
            uriRepository.releasePersistablePermission(uri)
        } catch (e: UriError.ReleasePermission) {
            val error = ReleaseUriPermissionError.ReleaseUriPermission(e)
            Log.e(logTag, "${logMsg}失敗", error)
            return UseCaseResult.Error(error)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
