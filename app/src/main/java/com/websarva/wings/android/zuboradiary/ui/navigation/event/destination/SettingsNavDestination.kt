package com.websarva.wings.android.zuboradiary.ui.navigation.event.destination

import com.websarva.wings.android.zuboradiary.ui.model.message.SettingsAppMessage
import java.time.DayOfWeek

/**
 * 設定画面における画面遷移先を表す。
 *
 * 各サブクラスは、遷移先の画面やダイアログと、それに必要な引数を表す。
 */
sealed interface SettingsNavDestination : AppNavDestination {

    /**
     * アプリケーションメッセージダイアログ（情報、警告、エラーなどを表示する）。
     *
     * @property message 表示するメッセージデータ。
     */
    data class AppMessageDialog(val message: SettingsAppMessage) : SettingsNavDestination

    /** テーマカラー選択ダイアログ。 */
    data object ThemeColorPickerDialog : SettingsNavDestination

    /**
     * カレンダー開始曜日選択ダイアログ。
     * @property dayOfWeek 現在設定されている週の開始曜日。
     */
    data class CalendarStartDayPickerDialog(val dayOfWeek: DayOfWeek) : SettingsNavDestination

    /** リマインダー通知時間選択ダイアログ。 */
    data object ReminderNotificationTimePickerDialog : SettingsNavDestination

    /** 全日記削除確認ダイアログ。 */
    data object AllDiariesDeleteDialog : SettingsNavDestination

    /** 全設定初期化確認ダイアログ。 */
    data object AllSettingsInitializationDialog : SettingsNavDestination

    /** 全データ削除確認ダイアログ。 */
    data object AllDataDeleteDialog : SettingsNavDestination

    /** OSSライセンスダイアログ。 */
    data object OSSLicensesDialog : SettingsNavDestination

    /** 通知権限要求の理由説明ダイアログ。 */
    data object NotificationPermissionRationaleDialog : SettingsNavDestination

    /** 位置情報権限要求の理由説明ダイアログ。 */
    data object LocationPermissionRationaleDialog : SettingsNavDestination
}
