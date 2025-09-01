package com.websarva.wings.android.zuboradiary.domain.model.settings

/**
 * 天気情報取得設定を表すデータクラス。
 *
 * このクラスは、ユーザーが天気情報の自動取得を有効にしているかどうかを保持する。
 *
 * @property isEnabled 天気情報の自動取得が有効な場合は `true`、無効な場合は `false`。
 *                     デフォルトは `false`。
 */
internal class WeatherInfoFetchSetting(
    val isEnabled: Boolean = false
) : UserSetting
