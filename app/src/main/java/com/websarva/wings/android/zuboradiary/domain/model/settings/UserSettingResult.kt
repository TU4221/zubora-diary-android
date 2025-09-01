package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException

/**
 * UseCaseによってビジネスルールが適用された後の、ユーザー設定に関する最終的な結果。
 */
internal sealed class UserSettingResult<out T : UserSetting> {

    /**
     * 有効な設定値が利用可能であることを示す。
     * データが正常に取得できた場合、またはデータが見つからなかったが
     * ドメインルールによりデフォルト値が適用された場合にこの状態となる。
     */
    data class Success<out T : UserSetting>(val setting: T) : UserSettingResult<T>()

    /**
     * 設定値の取得にドメインレベルのエラーが発生したが、
     * そのエラーに対応するドメイン定義のフォールバック値が提供される。
     */
    data class Failure<out T : UserSetting>(
        val exception: UserSettingsLoadException,
        val fallbackSetting: T
    ) : UserSettingResult<T>()
}
