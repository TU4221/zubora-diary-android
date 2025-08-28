package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LoadWeatherInfoFetchSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingResult<WeatherInfoFetchSetting>>> {
        val logMsg = "天気情報取得設定読込_"
        Log.i(logTag, "${logMsg}開始")

        val value =
            settingsRepository
                .loadWeatherInfoFetchPreference()
                .map { result: UserSettingDataSourceResult<WeatherInfoFetchSetting> ->
                    when (result) {
                        is UserSettingDataSourceResult.Success -> {
                            UserSettingResult.Success(result.setting)
                        }
                        is UserSettingDataSourceResult.Failure -> {
                            val defaultSettingValue = WeatherInfoFetchSetting()
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
