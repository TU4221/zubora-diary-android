package com.websarva.wings.android.zuboradiary.ui.diary.calendar

import com.websarva.wings.android.zuboradiary.ui.common.event.UiEvent
import java.time.LocalDate

/**
 * カレンダー画面における、UIイベント。
 */
sealed interface CalendarUiEvent : UiEvent {

    /**
     * 指定された日付までカレンダーをスクロールさせることを示すイベント。
     * @property date スクロール先の年月。
     */
    data class ScrollCalendar(val date: LocalDate) : CalendarUiEvent

    /**
     * 指定された日付までカレンダーをスムーズにスクロールさせることを示すイベント。
     * @property date スクロール先の年月。
     */
    data class SmoothScrollCalendar(val date: LocalDate) : CalendarUiEvent
}
