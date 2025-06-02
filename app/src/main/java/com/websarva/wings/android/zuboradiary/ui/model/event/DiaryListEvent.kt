package com.websarva.wings.android.zuboradiary.ui.model.event

import android.net.Uri
import java.time.LocalDate
import java.time.Year

internal sealed class DiaryListEvent : ViewModelEvent() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryListEvent()
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryListEvent()
    data object NavigateWordSearchFragment : DiaryListEvent()
    data class NavigateStartYearMonthPickerDialog(
        val newestYear: Year,
        val oldestYear: Year
    ) : DiaryListEvent()
    data class NavigateDiaryDeleteDialog(val date: LocalDate, val uri: Uri?) : DiaryListEvent()
}
