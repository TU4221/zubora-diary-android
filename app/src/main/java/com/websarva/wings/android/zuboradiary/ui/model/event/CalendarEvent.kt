package com.websarva.wings.android.zuboradiary.ui.model.event

import com.websarva.wings.android.zuboradiary.ui.model.DiaryIdUi
import java.time.LocalDate

sealed class CalendarEvent : UiEvent {
    internal data class NavigateDiaryEditFragment(
        val id: DiaryIdUi?,
        val date: LocalDate
    ) : CalendarEvent()
    internal data class ScrollCalendar(val date: LocalDate) : CalendarEvent()
    internal data class SmoothScrollCalendar(val date: LocalDate) : CalendarEvent()
    internal data class UpdateCalendarDayDotVisibility(
        val date: LocalDate,
        val isVisible: Boolean
    ) : CalendarEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : CalendarEvent()
}
