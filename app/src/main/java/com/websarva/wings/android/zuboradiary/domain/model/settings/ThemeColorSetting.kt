package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor

/**
 * アプリケーションのテーマカラー設定を表すデータクラス。
 *
 * このクラスは、ユーザーが選択したアプリケーションのテーマカラーを保持する。
 *
 * @property themeColor 選択されているテーマカラー。デフォルトは [ThemeColor.entries] の最初の要素。
 */
internal data class ThemeColorSetting(
    val themeColor: ThemeColor = ThemeColor.entries[0]
) : UserSetting
