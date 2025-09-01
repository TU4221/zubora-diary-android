package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException

/**
 * ユーザー設定のデータ取得結果を表す基底クラス。
 *
 * このクラスは、データソースから指定したユーザー設定の取得が成功したか失敗したかを示す。
 * 成功した場合はその設定値を、失敗した場合は関連する例外を保持する。
 *
 * @param T [UserSetting] を実装するユーザー設定の型。
 */
internal sealed class UserSettingDataSourceResult<out T : UserSetting> {

    /**
     * データソースから設定値が正常に取得できたことを示す。
     *
     * @param T [UserSetting] を実装するユーザー設定の型。
     * @property setting 取得されたユーザー設定。
     */
    data class Success<out T : UserSetting>(val setting: T) : UserSettingDataSourceResult<T>()

    /**
     * データソースからの設定値取得に失敗したことを示す。
     *
     * @param T [UserSetting] を実装するユーザー設定の型。
     * @property exception 設定値の取得に失敗した際に発生した例外。
     */
    data class Failure<out T : UserSetting>(
        val exception: UserSettingsLoadException
    ) : UserSettingDataSourceResult<T>()
}
