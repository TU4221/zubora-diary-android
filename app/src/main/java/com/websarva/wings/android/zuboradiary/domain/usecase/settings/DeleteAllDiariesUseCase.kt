package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteAllDiariesFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDiariesUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DeleteAllDiariesUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseAllUriPermissionUseCase: ReleaseAllUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult<Unit, DeleteAllDiariesUseCaseException> {
        val logMsg = "全日記削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllDiaries()
            releaseAllPictureUriPermission()
        } catch (e: DeleteAllDiariesUseCaseException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(DeleteAllDiariesUseCaseException.DeleteAllDiariesFailed::class)
    private suspend fun deleteAllDiaries() {
        val logMsg = "全日記データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: DeleteAllDiariesFailedException) {
            throw DeleteAllDiariesUseCaseException.DeleteAllDiariesFailed(e)
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DeleteAllDiariesUseCaseException.RevokeAllPersistentAccessUriFailed::class)
    private fun releaseAllPictureUriPermission() {
        val logMsg = "全画像Uri権限解放_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = releaseAllUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw DeleteAllDiariesUseCaseException
                    .RevokeAllPersistentAccessUriFailed(result.exception)
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
