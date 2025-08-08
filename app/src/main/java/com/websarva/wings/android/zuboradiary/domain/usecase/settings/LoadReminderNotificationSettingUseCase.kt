package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LoadReminderNotificationSettingUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingResult<ReminderNotificationSetting>>> {
        val logMsg = "リマインダー通知設定読込_"
        Log.i(logTag, "${logMsg}開始")

        val value =
            userPreferencesRepository
                .loadReminderNotificationPreference()
                .map { result: UserSettingDataSourceResult<ReminderNotificationSetting> ->
                    when (result) {
                        is UserSettingDataSourceResult.Success -> {
                            UserSettingResult.Success(result.setting)
                        }
                        is UserSettingDataSourceResult.Failure -> {
                            val defaultSettingValue = ReminderNotificationSetting.Disabled
                            when (result.exception) {
                                is UserSettingsException.AccessFailure -> {
                                    UserSettingResult.Failure(
                                        result.exception,
                                        defaultSettingValue
                                    )
                                }
                                is UserSettingsException.DataNotFound -> {
                                    UserSettingResult.Success(
                                        defaultSettingValue
                                    )
                                }
                            }
                        }
                    }
                }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
