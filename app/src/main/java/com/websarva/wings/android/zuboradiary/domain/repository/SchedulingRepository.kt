package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import java.time.LocalTime

/**
 * アプリケーションのスケジュール設定に関連する操作を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、リマインダー通知の登録とキャンセル機能を提供します。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外([DomainException] のサブクラス) をスローする。
 */
internal interface SchedulingRepository {

    /**
     * 指定された時刻にリマインダー通知をスケジュール登録する。
     *
     * @param settingTime リマインダー通知を設定する時刻。
     * @throws ReminderNotificationRegistrationFailureException リマインダー通知の登録に失敗した場合。
     */
    fun registerReminderNotification(settingTime: LocalTime)

    /**
     * 現在スケジュールされているリマインダー通知をキャンセルする。
     *
     * @throws ReminderNotificationCancellationFailureException リマインダー通知のキャンセルに失敗した場合。
     */
    fun cancelReminderNotification()
}
