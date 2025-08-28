package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import kotlinx.coroutines.flow.Flow

internal interface SettingsRepository {

    fun loadThemeColorPreference(): Flow<UserSettingDataSourceResult<ThemeColorSetting>>

    fun loadCalendarStartDayOfWeekPreference():
            Flow<UserSettingDataSourceResult<CalendarStartDayOfWeekSetting>>

    fun loadReminderNotificationPreference():
            Flow<UserSettingDataSourceResult<ReminderNotificationSetting>>

    fun loadPasscodeLockPreference():
            Flow<UserSettingDataSourceResult<PasscodeLockSetting>>

    fun loadWeatherInfoFetchPreference():
            Flow<UserSettingDataSourceResult<WeatherInfoFetchSetting>>

    @Throws(ThemeColorSettingUpdateFailureException::class)
    suspend fun saveThemeColorPreference(setting: ThemeColorSetting)

    @Throws(CalendarStartDayOfWeekSettingUpdateFailureException::class)
    suspend fun saveCalendarStartDayOfWeekPreference(setting: CalendarStartDayOfWeekSetting)

    @Throws(ReminderNotificationSettingUpdateFailureException::class)
    suspend fun saveReminderNotificationPreference(setting: ReminderNotificationSetting)

    @Throws(PassCodeSettingUpdateFailureException::class)
    suspend fun savePasscodeLockPreference(setting: PasscodeLockSetting)

    @Throws(WeatherInfoFetchSettingUpdateFailureException::class)
    suspend fun saveWeatherInfoFetchPreference(setting: WeatherInfoFetchSetting)
}
