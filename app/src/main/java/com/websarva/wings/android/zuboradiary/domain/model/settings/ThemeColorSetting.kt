package com.websarva.wings.android.zuboradiary.domain.model.settings

/**
 * アプリケーションのテーマカラー設定を表すデータクラス。
 *
 * このクラスは、ユーザーが選択したアプリケーションのテーマカラーを保持する。
 *
 * @property themeColor 選択されているテーマカラー。
 */
internal data class ThemeColorSetting(
    val themeColor: ThemeColor
) : UserSetting {
    companion object {
        /**
         * デフォルトのテーマカラー設定 (ホワイト) を返す。
         *
         * @return デフォルトのテーマカラー設定。
         */
        fun default(): ThemeColorSetting {
            return ThemeColorSetting(ThemeColor.entries[0])
        }
    }
}
