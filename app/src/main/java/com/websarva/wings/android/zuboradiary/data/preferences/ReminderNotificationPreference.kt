package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * リマインダー通知機能に関するユーザー設定を表すデータクラス。
 *
 * この設定は、リマインダー通知が有効かどうか、および通知する時刻を保持する。
 *
 * @property isEnabled リマインダー通知機能が有効な場合はtrue、無効な場合はfalse。
 * @property notificationTimeString 通知を送信する時刻を表す文字列。
 *                                  通知が無効な場合や未設定の場合は、空文字列を代入。
 */
internal class ReminderNotificationPreference(
    val isEnabled: Boolean,
    val notificationTimeString: String
) : UserPreference
