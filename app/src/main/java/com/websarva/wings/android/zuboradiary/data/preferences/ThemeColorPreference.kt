package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * アプリケーションのテーマカラーに関するユーザー設定を表すデータクラス。
 *
 * この設定は、ユーザーが選択したアプリのテーマカラーを識別するための番号を保持する。
 *
 * @property themeColorNumber 選択されたテーマカラーを表す整数値。
 */
internal class ThemeColorPreference(
    val themeColorNumber: Int
) : UserPreference
