package com.websarva.wings.android.zuboradiary.domain.model.settings

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * カレンダーの開始曜日設定を表すデータクラス。
 *
 * このクラスは、ユーザーがカレンダー表示の開始曜日としてどの曜日を選択しているかを保持する。
 *
 * @property dayOfWeek 選択されている開始曜日。
 */
@Serializable
internal data class CalendarStartDayOfWeekSetting(
    val dayOfWeek: DayOfWeek
) : UserSetting{

    companion object {
        /**
         * デフォルトのカレンダー開始曜日設定（日曜日）を返す。
         *
         * @return デフォルトのカレンダー開始曜日設定。
         */
        fun default(): CalendarStartDayOfWeekSetting {
            return CalendarStartDayOfWeekSetting(DayOfWeek.SUNDAY)
        }
    }
}
