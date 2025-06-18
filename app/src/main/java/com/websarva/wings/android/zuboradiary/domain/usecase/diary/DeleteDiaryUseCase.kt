package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.net.Uri
import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.DeleteDiaryError
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class DeleteDiaryUseCase(
    private val diaryRepository: DiaryRepository,
    private val releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
) {

    private val logTag = createLogTag()

    // MEMO:日記表示、編集フラグメント以外からも削除できるように下記引数とする。
    suspend operator fun invoke(
        loadedDate: LocalDate,
        loadedPicturePath: Uri?
    ): UseCaseResult<Unit, DeleteDiaryError> {
        val logMsg = "日記削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            deleteDiary(loadedDate)
            if (loadedPicturePath != null) releasePictureUriPermission(loadedPicturePath)
        } catch (e: DeleteDiaryError) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Error(e)
        }

        Log.e(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }

    private suspend fun deleteDiary(
        date: LocalDate
    ) {
        val logMsg = "日記データ削除_"
        Log.i(logTag, "${logMsg}開始")

        try {
            diaryRepository.deleteDiary(date)
        } catch (e: Exception) {
            throw DeleteDiaryError.DeleteDiary(e)
        }

        Log.i(logTag, "${logMsg}完了")
    }

    private suspend fun releasePictureUriPermission(
        picturePath: Uri
    ) {
        val logMsg = "画像Uri権限解放_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = releaseUriPermissionUseCase(picturePath)) {
            is UseCaseResult.Success -> {
                // 処理なし
            }
            is UseCaseResult.Error -> {
                throw DeleteDiaryError.ReleaseUriPermission(result.error)
            }
        }

        Log.i(logTag, "${logMsg}完了")
    }
}
