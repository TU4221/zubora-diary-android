package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * アプリケーションの初回起動フラグを表すデータクラス。
 *
 * この設定は、アプリケーションが初回起動であるかどうかを定義する。
 *
 * @property isFirstLaunch 初回起動である場合はtrue、そうでない場合はfalse。
 */
internal class IsFirstLaunchPreference(
    val isFirstLaunch: Boolean
) : UserPreference
