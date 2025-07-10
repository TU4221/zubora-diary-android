package com.websarva.wings.android.zuboradiary.data.mapper.preference

import com.websarva.wings.android.zuboradiary.data.preferences.PasscodeLockPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting

internal fun PasscodeLockPreference.toDomainModel(): PasscodeLockSetting {
    return if (isChecked) {
        PasscodeLockSetting.Enabled(passcode)
    } else {
        PasscodeLockSetting.Disabled
    }
}

internal fun PasscodeLockSetting.toDataModel(): PasscodeLockPreference {
    return when (this) {
        is PasscodeLockSetting.Enabled -> PasscodeLockPreference(isChecked, passcode)
        PasscodeLockSetting.Disabled -> PasscodeLockPreference(isChecked)
    }
}
