package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsAccessException

internal sealed class UserSettingFlowResult<out T : UserSetting> {
    data class Success<out T : UserSetting>(val setting: T) : UserSettingFlowResult<T>()
    data class Failure<out T : UserSetting>(
        val exception: UserSettingsAccessException,
        val fallbackSetting: T
    ) : UserSettingFlowResult<T>()
}
