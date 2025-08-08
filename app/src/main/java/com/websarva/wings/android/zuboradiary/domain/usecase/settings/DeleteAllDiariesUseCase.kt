package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDiariesDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDiariesUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DeleteAllDiariesUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult<Unit, DeleteAllDiariesUseCaseException> {
        val logMsg = "全日記削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllDiaries()
            releaseAllImageUriPermission()
        } catch (e: DeleteAllDiariesUseCaseException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(DeleteAllDiariesUseCaseException.AllDiariesDeleteFailure::class)
    private suspend fun deleteAllDiaries() {
        val logMsg = "全日記データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteAllDiaries()
        } catch (e: AllDiariesDeleteFailureException) {
            throw DeleteAllDiariesUseCaseException.AllDiariesDeleteFailure(e)
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DeleteAllDiariesUseCaseException.AllPersistableUriPermissionReleaseFailure::class)
    private fun releaseAllImageUriPermission() {
        val logMsg = "全永続的URI権限解放_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = releaseAllPersistableUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw DeleteAllDiariesUseCaseException
                    .AllPersistableUriPermissionReleaseFailure(result.exception)
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
