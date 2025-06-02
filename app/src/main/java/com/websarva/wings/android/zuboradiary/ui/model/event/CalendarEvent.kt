package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate

internal sealed class CalendarEvent : ViewModelEvent() {
    data class NavigateDiaryEditFragment(
        val date: LocalDate,
        val isNewDiary: Boolean
    ) : CalendarEvent()
    data class LoadDiary(val date: LocalDate) : CalendarEvent()
    data object InitializeDiary : CalendarEvent()
    data class ScrollCalendar(val date: LocalDate) : CalendarEvent()
    data class SmoothScrollCalendar(val date: LocalDate) : CalendarEvent()
}
