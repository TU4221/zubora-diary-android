package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.DayOfWeek

/**
 * カレンダーの開始曜日設定の更新処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param dayOfWeek 更新しようとしたカレンダーの開始曜日。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class CalendarStartDayOfWeekSettingUpdateFailureException(
    dayOfWeek: DayOfWeek,
    cause: Throwable
) : DomainException("カレンダー開始曜日設定 '$dayOfWeek' の更新に失敗しました。", cause)
