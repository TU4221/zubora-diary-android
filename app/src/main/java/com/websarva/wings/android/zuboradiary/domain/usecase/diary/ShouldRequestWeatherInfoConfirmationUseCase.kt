package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldRequestWeatherInfoConfirmationUseCase(
    val shouldLoadWeatherInfoUseCase: ShouldLoadWeatherInfoUseCase
) {

    private val logTag = createLogTag()

    operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?
    ): UseCaseResult<Boolean, Nothing> {
        val logMsg = "天気情報取得確認要求確認_"
        Log.i(logTag, "${logMsg}開始")

        if (previousDate == null) {
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(false)
        }

        when (val result = shouldLoadWeatherInfoUseCase(inputDate, previousDate)) {
            is UseCaseResult.Success -> {
                Log.i(logTag, "${logMsg}完了")
                return UseCaseResult.Success(result.value)
            }
            is UseCaseResult.Error -> {
                // 処理不要
            }
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(false)
    }
}
