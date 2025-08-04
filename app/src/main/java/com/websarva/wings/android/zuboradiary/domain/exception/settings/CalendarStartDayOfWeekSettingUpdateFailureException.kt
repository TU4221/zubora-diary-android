package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.DayOfWeek

internal class CalendarStartDayOfWeekSettingUpdateFailureException(
    dayOfWeek: DayOfWeek,
    cause: Throwable
) : DomainException("カレンダー開始曜日設定 '$dayOfWeek' の更新に失敗しました。", cause)
