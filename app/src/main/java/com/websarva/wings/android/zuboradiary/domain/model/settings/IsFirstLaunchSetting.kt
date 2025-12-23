package com.websarva.wings.android.zuboradiary.domain.model.settings

/**
 * アプリケーションの初回起動フラグを表すデータクラス。
 *
 * このクラスは、アプリケーションが初回起動であるかどうかを保持する。
 *
 * @property isFirstLaunch 初回起動である場合は `true`、そうでない場合は `false`。
 */
internal data class IsFirstLaunchSetting(
    val isFirstLaunch: Boolean
) : UserSetting {
    companion object {
        /**
         * デフォルトの初回起動設定（初回起動とみなす）を返す。
         *
         * @return デフォルトの初回起動設定。
         */
        fun default(): IsFirstLaunchSetting {
            return IsFirstLaunchSetting(true)
        }
    }
}
