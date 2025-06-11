package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult2
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldRequestDiaryUpdateConfirmationUseCase(
    private val doesDiaryExistUseCase: DoesDiaryExistUseCase
) {

    private val logTag = createLogTag()
    
    sealed class ShouldRequestDiaryUpdateConfirmationUseCaseException(
        message: String,
        cause: Throwable? = null
    ) : UseCaseException(message, cause) {
        
        class CheckFailedException(
            cause: Throwable?
        ) : ShouldRequestDiaryUpdateConfirmationUseCaseException(
            "日記更新確認要求確認に失敗しました。",
            cause
        )
    }

    suspend operator fun invoke(
        inputDate: LocalDate,
        loadedDate: LocalDate?
    ): UseCaseResult2<Boolean, ShouldRequestDiaryUpdateConfirmationUseCaseException> {
        val logMsg = "日記更新確認必要確認_"
        Log.i(logTag, "${logMsg}開始")

        if (inputDate == loadedDate) return UseCaseResult2.Success(false)

        return when (val result = doesDiaryExistUseCase(inputDate)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                UseCaseResult2.Success(result.value)
            }
            is UseCaseResult.Error -> {
                Log.e(logTag, "${logMsg}失敗")
                UseCaseResult2.Error(
                    ShouldRequestDiaryUpdateConfirmationUseCaseException
                        .CheckFailedException(result.exception)
                )
            }
        }
    }
}
