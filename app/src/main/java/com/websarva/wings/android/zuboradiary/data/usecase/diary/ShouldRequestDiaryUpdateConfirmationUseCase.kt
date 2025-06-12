package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.usecase.diary.error.ShouldRequestDiaryUpdateConfirmationError
import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldRequestDiaryUpdateConfirmationUseCase(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        inputDate: LocalDate,
        loadedDate: LocalDate?
    ): UseCaseResult<Boolean, ShouldRequestDiaryUpdateConfirmationError> {
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
                val error =
                    ShouldRequestDiaryUpdateConfirmationError
                        .CheckDiaryExistence(result.error)
                Log.e(logTag, "${logMsg}失敗", error)
                UseCaseResult.Error(error)
            }
        }
    }
}
