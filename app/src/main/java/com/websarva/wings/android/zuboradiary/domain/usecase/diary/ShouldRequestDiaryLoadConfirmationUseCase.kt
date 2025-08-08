package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldRequestDiaryLoadConfirmationUseCase(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?,
        originalDate: LocalDate,
        isNewDiary: Boolean
    ): DefaultUseCaseResult<Boolean> {
        val logMsg = "日記読込確認必要確認_"
        Log.i(logTag, "${logMsg}開始")

        if (inputDate == previousDate) {
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(false)
        }
        if (!isNewDiary && inputDate == originalDate) {
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(false)
        }

        return when (val result = doesDiaryExistUseCase(inputDate)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                UseCaseResult.Success(result.value)
            }
            is UseCaseResult.Failure -> {
                Log.e(logTag, "${logMsg}失敗", result.exception)
                UseCaseResult.Failure(result.exception)
            }
        }
    }
}
