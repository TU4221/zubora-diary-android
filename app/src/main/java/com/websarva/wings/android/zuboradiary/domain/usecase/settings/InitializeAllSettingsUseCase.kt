package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class InitializeAllSettingsUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): DefaultUseCaseResult<Unit> {
        val logMsg = "全設定初期化_"
        Log.i(logTag, "${logMsg}開始")

        try {
            userPreferencesRepository.saveThemeColorPreference(ThemeColorSetting())
            userPreferencesRepository.saveCalendarStartDayOfWeekPreference(
                CalendarStartDayOfWeekSetting()
            )
            userPreferencesRepository.saveReminderNotificationPreference(
                ReminderNotificationSetting.Disabled
            )
            userPreferencesRepository.savePasscodeLockPreference(
                PasscodeLockSetting.Disabled
            )
            userPreferencesRepository.saveWeatherInfoFetchPreference(
                WeatherInfoFetchSetting()
            )
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗")
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
