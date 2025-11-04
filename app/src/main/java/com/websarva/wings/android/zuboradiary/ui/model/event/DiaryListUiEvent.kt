package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate
import java.time.Year

sealed class DiaryListUiEvent : UiEvent {
    data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryListUiEvent()
    data class NavigateDiaryEditFragment(val id: String? = null, val date: LocalDate) : DiaryListUiEvent()
    data object NavigateWordSearchFragment : DiaryListUiEvent()
    data class NavigateStartYearMonthPickerDialog(
        val newestYear: Year,
        val oldestYear: Year
    ) : DiaryListUiEvent()
    data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryListUiEvent()
}
