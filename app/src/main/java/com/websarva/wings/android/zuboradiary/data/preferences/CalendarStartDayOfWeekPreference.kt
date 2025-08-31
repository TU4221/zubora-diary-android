package com.websarva.wings.android.zuboradiary.data.preferences

/**
 * カレンダーの開始曜日に関するユーザー設定を表すデータクラス。
 *
 * この設定は、カレンダービューの週の始まりを何曜日にするかを定義する。
 *
 * @property dayOfWeekNumber 週の開始曜日を表す整数値。
 */
internal data class CalendarStartDayOfWeekPreference(
    val dayOfWeekNumber: Int
) : UserPreference
