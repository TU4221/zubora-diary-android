package com.websarva.wings.android.zuboradiary.ui.model.action

import java.time.LocalDate

internal sealed class CalendarFragmentAction : FragmentAction() {
    data class NavigateDiaryEditFragment(
        val date: LocalDate,
        val isNewDiary: Boolean
    ) : CalendarFragmentAction()
    data class LoadDiary(val date: LocalDate) : CalendarFragmentAction()
    data object InitializeDiary : CalendarFragmentAction()
    data class ScrollCalendar(val date: LocalDate) : CalendarFragmentAction()
    data class SmoothScrollCalendar(val date: LocalDate) : CalendarFragmentAction()
}
