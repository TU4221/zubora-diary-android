package com.websarva.wings.android.zuboradiary.ui.settings

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent

/**
 * 設定画面における、UIイベント。
 */
sealed interface SettingsUiEvent : UiEvent {

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
     * 天気情報取得設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnWeatherInfoFetchSettingSwitch(val isChecked: Boolean) : SettingsUiEvent
}
