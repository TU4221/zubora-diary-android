package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.preferences.IsFirstLaunchPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.IsFirstLaunchSetting

internal fun IsFirstLaunchPreference.toDomainModel(): IsFirstLaunchSetting {
    return IsFirstLaunchSetting(isFirstLaunch)
}

internal fun IsFirstLaunchSetting.toDataModel(): IsFirstLaunchPreference {
    return IsFirstLaunchPreference(isFirstLaunch)
}
