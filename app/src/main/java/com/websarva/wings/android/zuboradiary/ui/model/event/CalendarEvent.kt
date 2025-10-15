package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

sealed class CalendarEvent : UiEvent {
    internal data class NavigateDiaryEditFragment(
        val id: String?,
        val date: LocalDate
    ) : CalendarEvent()
    internal data class ScrollCalendar(val date: LocalDate) : CalendarEvent()
    internal data class SmoothScrollCalendar(val date: LocalDate) : CalendarEvent()
    internal data class RefreshCalendarDayDotVisibility(
        val date: LocalDate,
        val isVisible: Boolean
    ) : CalendarEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : CalendarEvent()
}
