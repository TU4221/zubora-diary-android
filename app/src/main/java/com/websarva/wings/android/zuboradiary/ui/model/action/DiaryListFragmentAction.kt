package com.websarva.wings.android.zuboradiary.ui.model.action

import android.net.Uri
import java.time.LocalDate
import java.time.Year

internal sealed class DiaryListFragmentAction : FragmentAction() {
    data class NavigateDiaryShowFragment(val date: LocalDate) : DiaryListFragmentAction()
    data class NavigateDiaryEditFragment(val date: LocalDate) : DiaryListFragmentAction()
    data object NavigateWordSearchFragment : DiaryListFragmentAction()
    data class NavigateStartYearMonthPickerDialog(
        val newestYear: Year,
        val oldestYear: Year
    ) : DiaryListFragmentAction()
    data class NavigateDiaryDeleteDialog(val date: LocalDate, val uri: Uri?) : DiaryListFragmentAction()
    data class ReleasePersistablePermissionUri(val uri: Uri) : DiaryListFragmentAction()
}
