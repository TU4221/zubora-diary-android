package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate
import java.time.Year

sealed class DiaryListEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryListEvent()
    internal data class NavigateDiaryEditFragment(val id: String? = null, val date: LocalDate) : DiaryListEvent()
    internal data object NavigateWordSearchFragment : DiaryListEvent()
    internal data class NavigateStartYearMonthPickerDialog(
        val newestYear: Year,
        val oldestYear: Year
    ) : DiaryListEvent()
    internal data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryListEvent()

    internal data class CommonEvent(val wrappedEvent: CommonUiEvent) : DiaryListEvent()
}
