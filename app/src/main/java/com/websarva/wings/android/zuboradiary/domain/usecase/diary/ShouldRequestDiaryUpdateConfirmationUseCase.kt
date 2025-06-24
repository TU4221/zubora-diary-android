package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.error.DiaryError
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldRequestDiaryUpdateConfirmationUseCase(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        inputDate: LocalDate,
        loadedDate: LocalDate?
    ): UseCaseResult<Boolean, DiaryError.CheckDiaryExistence> {
        val logMsg = "日記更新確認必要確認_"
        Log.i(logTag, "${logMsg}開始")

        if (inputDate == loadedDate) {
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(false)
        }

        return when (val result = doesDiaryExistUseCase(inputDate)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                UseCaseResult.Success(result.value)
            }
            is UseCaseResult.Error -> {
                Log.e(logTag, "${logMsg}失敗",result.error)
                UseCaseResult.Error(result.error)
            }
        }
    }
}
