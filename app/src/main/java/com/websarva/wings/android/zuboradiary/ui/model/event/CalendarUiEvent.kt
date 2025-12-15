package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.fragment.CalendarFragment
import java.time.LocalDate

/**
 * カレンダー画面([CalendarFragment])における、UIイベント。
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

    /**
     * カレンダーの日付に表示されるドット（日記有無の目印）の表示/非表示を更新することを示すイベント。
     * @property date 対象の日付。
     * @property isVisible ドットを表示する場合は`true`。
     */
    data class RefreshCalendarDayDotVisibility(
        val date: LocalDate,
        val isVisible: Boolean
    ) : CalendarUiEvent
}
