package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LoadThemeColorSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingResult<ThemeColorSetting>>> {
        val logMsg = "テーマカラー設定読込_"
        Log.i(logTag, "${logMsg}開始")

        val value =
            settingsRepository
                .loadThemeColorPreference()
                .map { result: UserSettingDataSourceResult<ThemeColorSetting> ->
                    when (result) {
                        is UserSettingDataSourceResult.Success -> {
                            UserSettingResult.Success(result.setting)
                        }
                        is UserSettingDataSourceResult.Failure -> {
                            val defaultSettingValue = ThemeColorSetting()
                            when (result.exception) {
                                is UserSettingsLoadException.AccessFailure -> {
                                    UserSettingResult.Failure(
                                        result.exception,
                                        defaultSettingValue
                                    )
                                }
                                is UserSettingsLoadException.DataNotFound -> {
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
