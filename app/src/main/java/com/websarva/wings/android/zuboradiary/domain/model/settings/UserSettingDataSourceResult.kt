package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException

/**
 * リポジトリから返される、ユーザー設定のデータ取得に関する直接的な結果。
 */
internal sealed class UserSettingDataSourceResult<out T : UserSetting> {

    /**
     * データソースから設定値が正常に取得できたことを示す。
     */
    data class Success<out T : UserSetting>(val setting: T) : UserSettingDataSourceResult<T>()

    /**
     * データソースからの設定値取得に失敗したことを示す。
     */
    data class Failure<out T : UserSetting>(
        val exception: UserSettingsLoadException
    ) : UserSettingDataSourceResult<T>()
}
