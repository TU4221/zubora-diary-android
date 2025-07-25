package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

sealed class CalendarEvent : ViewModelEvent {
    internal data class NavigateDiaryEditFragment(
        val date: LocalDate,
        val isNewDiary: Boolean
    ) : CalendarEvent()
    internal data class LoadDiary(val date: LocalDate) : CalendarEvent()
    internal data object InitializeDiary : CalendarEvent()
    internal data class ScrollCalendar(val date: LocalDate) : CalendarEvent()
    internal data class SmoothScrollCalendar(val date: LocalDate) : CalendarEvent()

    internal data class CommonEvent(val wrappedEvent: CommonViewModelEvent) : CalendarEvent()
}
