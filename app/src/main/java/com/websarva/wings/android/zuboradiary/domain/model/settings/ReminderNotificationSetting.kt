package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.core.serializer.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalTime

/**
 * リマインダー通知設定の状態を表す基底クララス。
 *
 * このクラスは、リマインダー通知が有効か無効か、そして有効な場合はその通知時刻を保持する。
 *
 * @property isEnabled リマインダー通知が有効な場合は `true`、無効な場合は `false`。
 */
@Serializable
internal sealed class ReminderNotificationSetting(
    val isEnabled: Boolean
) : UserSetting {

    /**
     * リマインダー通知が有効な状態を表すデータクラス。
     *
     * @property notificationTime 通知を行う時刻。
     */
    @Serializable
    data class Enabled(
        @Serializable(with = LocalTimeSerializer::class)
        val notificationTime: LocalTime
    ) : ReminderNotificationSetting(true)

    /**
     * リマインダー通知が無効な状態を表すデータオブジェクト。
     */
    @Serializable
    data object Disabled : ReminderNotificationSetting(false) {
    }

    companion object {
        /**
         * デフォルトのリマインダー通知設定（無効）を返す。
         *
         * @return デフォルトのリマインダー通知設定。
         */
        fun default(): ReminderNotificationSetting {
            return Disabled
        }
    }
}
