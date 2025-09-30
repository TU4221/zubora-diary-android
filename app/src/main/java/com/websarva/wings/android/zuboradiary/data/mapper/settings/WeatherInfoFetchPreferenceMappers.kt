package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoFetchPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting

internal fun WeatherInfoFetchPreference.toDomainModel(): WeatherInfoFetchSetting {
    return WeatherInfoFetchSetting(isEnabled)
}

internal fun WeatherInfoFetchSetting.toDataModel(): WeatherInfoFetchPreference {
    return WeatherInfoFetchPreference(isEnabled)
}
