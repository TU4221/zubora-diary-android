package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.DayOfWeek

internal class SaveCalendarStartDayOfWeekUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        dayOfWeek: DayOfWeek
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "カレンダー開始曜日設定保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val preferenceValue = CalendarStartDayOfWeekSetting(dayOfWeek)
            userPreferencesRepository.saveCalendarStartDayOfWeekPreference(preferenceValue)
        } catch (e: CalendarStartDayOfWeekSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗")
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
