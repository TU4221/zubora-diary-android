package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * 天気情報の取得機能に関するユーザー設定を表すデータクラス。
 *
 * この設定は、アプリケーションが天気情報を取得するかどうかを定義する。
 *
 * @property isEnabled 天気情報の取得機能が有効な場合はtrue、無効な場合はfalse。
 */
internal class WeatherInfoFetchPreference(
    val isEnabled: Boolean
) : UserPreference
