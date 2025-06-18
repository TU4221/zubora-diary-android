package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.Diary
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class ShouldRequestExitWithoutDiarySavingConfirmationUseCase {

    private val logTag = createLogTag()

    operator fun invoke(
        editedDiary: Diary,
        loadedDiary: Diary?
    ): UseCaseResult<Boolean, Nothing> {
        val logMsg = "日記未保存終了確認_"
        Log.i(logTag, "${logMsg}開始")

        val boolean =
            if (loadedDiary == null) {
                false
            } else {
                !editedDiary.isContentEqualToIgnoringLog(loadedDiary)
            }
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(boolean)
    }
}
