package com.websarva.wings.android.zuboradiary.ui.model.action

import java.time.LocalDate

internal sealed class CalendarFragmentAction : FragmentAction() {
    data class NavigateDiaryEditFragment(
        val date: LocalDate,
        val isNewDiary: Boolean
    ) : CalendarFragmentAction()
    data class ShowDiary(val date: LocalDate) : CalendarFragmentAction()
    data object CloseDiary : CalendarFragmentAction()
    data class ScrollCalendar(val date: LocalDate) : CalendarFragmentAction()
}
