package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.SettingsFragment
import java.time.DayOfWeek

/**
 * 設定画面([SettingsFragment])における、UIイベントを表すsealed class。
 */
sealed class SettingsUiEvent : UiEvent {

    /** テーマカラー選択ダイアログを表示することを示すイベント。 */
    data object ShowThemeColorPickerDialog : SettingsUiEvent()

    /**
     * カレンダー開始曜日選択ダイアログを表示することを示すイベント。
     * @property dayOfWeek 現在設定されている週の開始曜日。
     */
    data class ShowCalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsUiEvent()

    /** リマインダー通知時間選択ダイアログを表示することを示すイベント。 */
    data object ShowReminderNotificationTimePickerDialog : SettingsUiEvent()

    /** 通知権限要求の理由説明ダイアログを表示することを示すイベント。 */
    data object ShowNotificationPermissionDialog : SettingsUiEvent()

    /** 位置情報権限要求の理由説明ダイアログを表示することを示すイベント。 */
    data object ShowLocationPermissionDialog : SettingsUiEvent()

    /** 全日記削除確認ダイアログを表示することを示すイベント。 */
    data object ShowAllDiariesDeleteDialog : SettingsUiEvent()

    /** 全設定初期化確認ダイアログを表示することを示すイベント。 */
    data object ShowAllSettingsInitializationDialog : SettingsUiEvent()

    /** 全データ削除確認ダイアログを表示することを示すイベント。 */
    data object ShowAllDataDeleteDialog : SettingsUiEvent()

    /** OSSライセンスダイアログを表示することを示すイベント。 */
    data object ShowOSSLicensesDialog : SettingsUiEvent()

    /** 通知権限を要求するダイアログを表示することを示すイベント。 */
    data object ShowRequestPostNotificationsPermissionRationale : SettingsUiEvent()

    /** 位置情報権限を要求するダイアログを表示することを示すイベント。 */
    data object ShowRequestAccessLocationPermissionRationale : SettingsUiEvent()

    /** アプリケーションの詳細設定画面を表示することを示すイベント。 */
    data object ShowApplicationDetailsSettingsScreen : SettingsUiEvent()

    /** 通知権限を確認することを示すイベント。 */
    data object CheckPostNotificationsPermission : SettingsUiEvent()

    /** 通知権限要求の理由を表示する必要があるか確認することを示すイベント。 */
    data object CheckShouldShowRequestPostNotificationsPermissionRationale : SettingsUiEvent()

    /** 位置情報権限を確認することを示すイベント。 */
    data object CheckAccessLocationPermission : SettingsUiEvent()

    /** 位置情報権限要求の理由を表示する必要があるか確認することを示すイベント。 */
    data object CheckShouldShowRequestAccessLocationPermissionRationale : SettingsUiEvent()

    /**
     * リマインダー通知設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnReminderNotificationSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()

    /**
     * パスコードロック設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnPasscodeLockSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()

    /**
     * 天気情報取得設定のスイッチの状態を変更することを示すイベント。
     * @property isChecked スイッチをONにする場合は`true`。
     */
    data class TurnWeatherInfoFetchSettingSwitch(val isChecked: Boolean) : SettingsUiEvent()
}
