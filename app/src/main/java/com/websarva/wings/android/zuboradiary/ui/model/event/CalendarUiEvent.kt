package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

sealed class CalendarUiEvent : UiEvent {
    data class NavigateDiaryEditFragment(
        val id: String?,
        val date: LocalDate
    ) : CalendarUiEvent()
    data class ScrollCalendar(val date: LocalDate) : CalendarUiEvent()
    data class SmoothScrollCalendar(val date: LocalDate) : CalendarUiEvent()
    data class RefreshCalendarDayDotVisibility(
        val date: LocalDate,
        val isVisible: Boolean
    ) : CalendarUiEvent()
}
