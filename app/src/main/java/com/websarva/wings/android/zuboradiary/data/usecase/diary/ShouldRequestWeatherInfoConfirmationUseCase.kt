package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.usecase.diary.error.ShouldRequestWeatherInfoConfirmationError
import com.websarva.wings.android.zuboradiary.data.usecase.settings.IsWeatherInfoAcquisitionEnabledUseCase
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class ShouldRequestWeatherInfoConfirmationUseCase(
    private val isWeatherInfoAcquisitionEnabledUseCase: IsWeatherInfoAcquisitionEnabledUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        inputDate: LocalDate,
        previousDate: LocalDate?,
        loadedDate: LocalDate?
    ): UseCaseResult<Boolean, ShouldRequestWeatherInfoConfirmationError> {
        val logMsg = "天気情報取得確認要求確認_"
        Log.i(logTag, "${logMsg}開始")

        when (val result = isWeatherInfoAcquisitionEnabledUseCase()) {
            is UseCaseResult.Success -> {
                if (!result.value) {
                    Log.i(logTag, "${logMsg}完了")
                    return UseCaseResult.Success(false)
                }
            }
            is UseCaseResult.Error -> {
                val error = ShouldRequestWeatherInfoConfirmationError.LoadSettings(result.error)
                Log.e(logTag, "${logMsg}失敗", error)
                return UseCaseResult.Error(error)
            }
        }

        if (loadedDate != null && previousDate == null) {
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(false)
        }

        if (previousDate == inputDate) {
            Log.i(logTag, "${logMsg}完了")
            return UseCaseResult.Success(false)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(true)
    }
}
