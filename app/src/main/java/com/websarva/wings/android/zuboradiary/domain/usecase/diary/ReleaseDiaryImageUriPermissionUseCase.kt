package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DiaryImageUriUsageCheckFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ReleaseDiaryImageUriPermissionUseCase(
    private val diaryRepository: DiaryRepository,
    private val releasePersistableUriPermissionUseCase: ReleasePersistableUriPermissionUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(uriString: String): DefaultUseCaseResult<Unit> {
        val logMsg = "日記画像URIの永続的権限解放_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val existsImageUri = diaryRepository.existsImageUri(uriString)
            if (existsImageUri) return UseCaseResult.Success(Unit)
        } catch (e: DiaryImageUriUsageCheckFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        when (val result = releasePersistableUriPermissionUseCase(uriString)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗", result.exception)
                UseCaseResult.Failure(result.exception)
            }
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
