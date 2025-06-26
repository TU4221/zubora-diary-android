package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldLoadWeatherInfoUseCase {

    private val logTag = createLogTag()

    operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?
    ): UseCaseResult.Success<Boolean> {
        val logMsg = "天気情報取得要確認_"
        Log.i(logTag, "${logMsg}開始")

        val boolean = inputDate != previousDate

        Log.i(logTag, "${logMsg}完了_${boolean}")
        return UseCaseResult.Success(boolean)
    }
}
