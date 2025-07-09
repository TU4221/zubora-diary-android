package com.websarva.wings.android.zuboradiary.data.mapper.preference

import com.websarva.wings.android.zuboradiary.data.preferences.PasscodeLockPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting

internal fun PasscodeLockPreference.toDomainModel(): PasscodeLockSetting {
    return PasscodeLockSetting(isChecked, passcode)
}

internal fun PasscodeLockSetting.toDataModel(): PasscodeLockPreference {
    return PasscodeLockPreference(isChecked, passCode)
}
