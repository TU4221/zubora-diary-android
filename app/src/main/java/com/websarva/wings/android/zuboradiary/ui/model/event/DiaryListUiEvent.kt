package com.websarva.wings.android.zuboradiary.ui.model.event

import java.time.LocalDate
import java.time.Year

sealed class DiaryListUiEvent : UiEvent {
    internal data class NavigateDiaryShowFragment(val id: String, val date: LocalDate) : DiaryListUiEvent()
    internal data class NavigateDiaryEditFragment(val id: String? = null, val date: LocalDate) : DiaryListUiEvent()
    internal data object NavigateWordSearchFragment : DiaryListUiEvent()
    internal data class NavigateStartYearMonthPickerDialog(
        val newestYear: Year,
        val oldestYear: Year
    ) : DiaryListUiEvent()
    internal data class NavigateDiaryDeleteDialog(val date: LocalDate) : DiaryListUiEvent()
}
