package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.DeleteAllDataFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDataUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DeleteAllDataUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseAllUriPermissionUseCase: ReleaseAllUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult<Unit, DeleteAllDataUseCaseException> {
        val logMsg = "アプリ全データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllData()
            releaseAllPictureUriPermission()
        } catch (e: DeleteAllDataUseCaseException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(DeleteAllDataUseCaseException.DeleteAllDataFailed::class)
    private suspend fun deleteAllData() {
        val logMsg = "全データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteAllData()
        } catch (e: DeleteAllDataFailedException) {
            throw DeleteAllDataUseCaseException.DeleteAllDataFailed(e)
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DeleteAllDataUseCaseException.RevokeAllPersistentAccessUriFailed::class)
    private fun releaseAllPictureUriPermission() {
        val logMsg = "全画像Uri権限解放_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = releaseAllUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw DeleteAllDataUseCaseException
                    .RevokeAllPersistentAccessUriFailed(result.exception)
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
