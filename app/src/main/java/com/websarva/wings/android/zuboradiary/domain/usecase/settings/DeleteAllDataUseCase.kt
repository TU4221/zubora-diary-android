package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.exception.diary.AllDataDeleteFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.exception.DeleteAllDataUseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class DeleteAllDataUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase,
    private val initializeAllSettingsUseCase: InitializeAllSettingsUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult<Unit, DeleteAllDataUseCaseException> {
        val logMsg = "アプリ全データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteAllData()
            releaseAllImageUriPermission()
            initializeAllSettings()
        } catch (e: DeleteAllDataUseCaseException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    @Throws(DeleteAllDataUseCaseException.AllDataDeleteFailure::class)
    private suspend fun deleteAllData() {
        val logMsg = "全データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteAllData()
        } catch (e: AllDataDeleteFailureException) {
            throw DeleteAllDataUseCaseException.AllDataDeleteFailure(e)
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DeleteAllDataUseCaseException.AllPersistableUriPermissionReleaseFailure::class)
    private fun releaseAllImageUriPermission() {
        val logMsg = "全永続的URI権限解放_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = releaseAllPersistableUriPermissionUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw DeleteAllDataUseCaseException
                    .AllPersistableUriPermissionReleaseFailure(result.exception)
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }

    @Throws(DeleteAllDataUseCaseException.AllSettingsInitializationFailure::class)
    private suspend fun initializeAllSettings() {
        val logMsg = "全設定初期化_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = initializeAllSettingsUseCase()) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Failure -> {
                throw DeleteAllDataUseCaseException
                    .AllSettingsInitializationFailure(result.exception)
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
