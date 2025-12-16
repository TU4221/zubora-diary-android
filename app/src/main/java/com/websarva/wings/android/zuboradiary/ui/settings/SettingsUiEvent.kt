package com.websarva.wings.android.zuboradiary.ui.settings

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent

/**
 * 設定画面における、UIイベント。
 */
sealed interface SettingsUiEvent : UiEvent {

    // TODO:不要だが残しておく(最終的に削除)
    /** アプリケーションの詳細設定画面を表示することを示すイベント。 */
    data object ShowApplicationDetailsSettingsScreen : SettingsUiEvent

    /** 通知権限を確認することを示すイベント。 */
    data object CheckPostNotificationsPermission : SettingsUiEvent

    /** 位置情報権限を確認することを示すイベント。 */
    data object CheckAccessLocationPermission : SettingsUiEvent

    /**
     * リマインダー通知設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnReminderNotificationSettingSwitch(val isChecked: Boolean) : SettingsUiEvent

    /**
     * パスコードロック設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnPasscodeLockSettingSwitch(val isChecked: Boolean) : SettingsUiEvent

    /**
     * 天気情報取得設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnWeatherInfoFetchSettingSwitch(val isChecked: Boolean) : SettingsUiEvent
}
