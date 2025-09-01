package com.websarva.wings.android.zuboradiary.domain.model.settings

import java.time.DayOfWeek

/**
 * カレンダーの開始曜日設定を表すデータクラス。
 *
 * このクラスは、ユーザーがカレンダー表示の開始曜日としてどの曜日を選択しているかを保持する。
 * デフォルトの開始曜日は日曜日。
 *
 * @property dayOfWeek 選択されている開始曜日。デフォルトは [DayOfWeek.SUNDAY]。
 */
internal data class CalendarStartDayOfWeekSetting(
    val dayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
) : UserSetting
